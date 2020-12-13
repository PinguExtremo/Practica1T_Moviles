package com.example.practica1t.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.practica1t.Jsons.JsonPiscinas;
import com.example.practica1t.R;
import com.example.practica1t.common.AdaptadorPiscinas;
import com.example.practica1t.common.Piscinas;
import com.example.practica1t.services.JsonService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.practica1t.common.Constantes.DISTANCIA;
import static com.example.practica1t.common.Constantes.URL_MADRID;

public class ListViewPiscinas extends AppCompatActivity {
    private Button boton;
    private ListView listView;
    private ArrayList<Piscinas> localizaciones;
    private AdaptadorPiscinas mPiscinaAdapter;
    private OutputStreamWriter escritor;
    private InputStreamReader flujo;
    private BufferedReader lector;
    private String texto;
    private Boolean dialogos;
    private Double latitude;
    private Double longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view_piscinas);

        leerFicheroLocalizacion();

        boton = findViewById(R.id.boton);
        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (latitude == null && longitude == null) {
                    Toast.makeText(getApplicationContext(), "No guardaste la ubicacion", Toast.LENGTH_SHORT).show();
                } else {
                    Intent i = new Intent(ListViewPiscinas.this, PiscinasActivity.class);
                    startActivity(i);
                }
            }
        });
        listView = findViewById(R.id.listView);
        getPiscinas();

        dialogos = true;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                leerFichero();

                final Piscinas p = (Piscinas) listView.getItemAtPosition(position);
                AlertDialog.Builder dialogo = new AlertDialog.Builder(ListViewPiscinas.this);
                dialogo.setTitle("Guardar en Favoritos");
                dialogo.setMessage("Desea añadir a Favoritos la Piscina " + p.getName());
                dialogo.setCancelable(false);
                dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (texto == null) {
                            escribirFichero(p.getLocation().getLatitude(), p.getLocation().getAltitude(), p.getName());
                            AlertDialog.Builder dialogoNoExiste = new AlertDialog.Builder(ListViewPiscinas.this);
                            dialogoNoExiste.setMessage("Favorito Añadido");
                            dialogoNoExiste.show();
                        } else {
                            String[] palabras = p.getName().split("\\s+");
                            for (String palabra : palabras) {
                                if (texto.contains(palabra)) {

                                    dialogos = true;
                                } else {
                                    escribirFichero(p.getLocation().getLatitude(), p.getLocation().getAltitude(), p.getName());

                                    dialogos = false;
                                }
                            }
                            if (dialogos == true) {
                                AlertDialog.Builder dialogoExiste = new AlertDialog.Builder(ListViewPiscinas.this);
                                dialogoExiste.setMessage("Esta piscina ya esta guardada");
                                dialogoExiste.show();

                            } else if (dialogos == false) {
                                AlertDialog.Builder dialogoNoExiste = new AlertDialog.Builder(ListViewPiscinas.this);
                                dialogoNoExiste.setMessage("Favorito guardado");
                                dialogoNoExiste.show();
                            }
                        }

                    }
                });
                dialogo.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialogo.show();


            }
        });
    }

    public void leerFicheroLocalizacion() {
        try {
            flujo = new InputStreamReader(openFileInput("UbicacionGuardada.txt"));
            lector = new BufferedReader(flujo);
            String texto = lector.readLine();
            lector.close();
            flujo.close();
            String[] coords = texto.split(";");
            String latitud = coords[0];
            String longitud = coords[1];
            latitude = Double.parseDouble(latitud);
            longitude = Double.parseDouble(longitud);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void leerFichero() {
        try {
            flujo = new InputStreamReader(openFileInput("favoritos.txt"));
            lector = new BufferedReader(flujo);
            texto = lector.readLine();
            lector.close();
            flujo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void escribirFichero(Double latitude, Double longitude, String nombre) {
        try {
            escritor = new OutputStreamWriter(openFileOutput("favoritos.txt", Context.MODE_PRIVATE));
            if (texto != null) {
                escritor.write(texto);
            }
            escritor.write(nombre + " ; " + latitude + " ; " + longitude + " . ");
            escritor.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getPiscinas() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_MADRID)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JsonService apiPiscinas = retrofit.create(JsonService.class);

        apiPiscinas.getPiscinaLocation(latitude, longitude, DISTANCIA).enqueue(new Callback<JsonPiscinas>() {

            @Override
            public void onResponse(Call<JsonPiscinas> call, Response<JsonPiscinas> response) {
                if (response != null && response.body() != null) {
                    localizaciones = (ArrayList<Piscinas>) response.body().results;

                    mPiscinaAdapter = new AdaptadorPiscinas(ListViewPiscinas.this, localizaciones);
                    listView.setAdapter(mPiscinaAdapter);
                    mPiscinaAdapter.notifyDataSetChanged();

                    TextView textView = findViewById(R.id.textView);

                    if (listView.getCount() != 0) {
                        textView.setText("Pulse para añadir a favoritos");
                    } else {

                    }

                }

            }

            @Override
            public void onFailure(Call<JsonPiscinas> call, Throwable t) {
                System.out.println("Fallo en la Api");
            }
        });

    }
}