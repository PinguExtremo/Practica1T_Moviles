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

import com.example.practica1t.Jsons.JsonPiscinas;
import com.example.practica1t.Jsons.JsonPolideportivos;
import com.example.practica1t.R;
import com.example.practica1t.common.AdaptadorPiscinas;
import com.example.practica1t.common.AdaptadorPolideportivos;
import com.example.practica1t.common.Piscinas;
import com.example.practica1t.common.Polideportivos;
import com.example.practica1t.services.JsonService;

import java.io.BufferedReader;
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

import static com.example.practica1t.common.Constantes.URL_MADRID;

public class ListViewPolideportivos extends AppCompatActivity {

    Button boton;
    ListView listView;
    ArrayList<Polideportivos> localizaciones;
    AdaptadorPolideportivos mPolideportivosAdapter;
    OutputStreamWriter escritor;
    InputStreamReader flujo;
    BufferedReader lector;
    String texto;
    Boolean dialogos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view_polideportivos);

        boton = findViewById(R.id.boton);
        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ListViewPolideportivos.this, PolideportivosActivity.class);
                startActivity(i);
            }
        });
        listView = findViewById(R.id.listView);
        getPolideportivos();
        crearFichero();
        dialogos = true;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                leerFichero();
                final Polideportivos p = (Polideportivos) listView.getItemAtPosition(position);
                AlertDialog.Builder dialogo = new AlertDialog.Builder(ListViewPolideportivos.this);
                dialogo.setTitle("titulo");
                dialogo.setMessage("¿Quieres añadir " + p.getName() + " a la lista de favoritos?");
                dialogo.setCancelable(false);
                dialogo.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (texto == null) {
                            escribirFichero(p.getLocation().getLatitude(), p.getLocation().getAltitude(), p.getName());
                            AlertDialog.Builder dialogoNoExiste = new AlertDialog.Builder(ListViewPolideportivos.this);
                            dialogoNoExiste.setMessage("FAVORITO GUARDADO");
                            dialogoNoExiste.show();
                        } else {
                            int index = texto.indexOf(p.getName());
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
                                AlertDialog.Builder dialogoExiste = new AlertDialog.Builder(ListViewPolideportivos.this);
                                dialogoExiste.setMessage("YA LO GUARDASTE ANTERIORMENTE");
                                dialogoExiste.show();

                            } else if (dialogos == false) {
                                AlertDialog.Builder dialogoNoExiste = new AlertDialog.Builder(ListViewPolideportivos.this);
                                dialogoNoExiste.setMessage("FAVORITO GUARDADO");
                                dialogoNoExiste.show();
                            }
                        }

                    }
                });
                dialogo.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialogo.show();
            }
        });
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

    public void crearFichero() {
        try {
            escritor = new OutputStreamWriter(openFileOutput("favoritos.txt", Context.MODE_PRIVATE));
            escritor.close();
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

    public void getPolideportivos() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_MADRID)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JsonService apiPolideportivos = retrofit.create(JsonService.class);

        apiPolideportivos.getPolideportivoLocation(40.4167, -3.70325, 4000).enqueue(new Callback<JsonPolideportivos>() {

            @Override
            public void onResponse(Call<JsonPolideportivos> call, Response<JsonPolideportivos> response) {
                if (response != null && response.body() != null) {
                    localizaciones = (ArrayList<Polideportivos>) response.body().results;

                    System.out.println("ESTO ES UN SOUT: " + response);
                    mPolideportivosAdapter = new AdaptadorPolideportivos(ListViewPolideportivos.this, localizaciones);
                    listView.setAdapter(mPolideportivosAdapter);
                    mPolideportivosAdapter.notifyDataSetChanged();

                }

            }

            @Override
            public void onFailure(Call<JsonPolideportivos> call, Throwable t) {
                System.out.println("failure");
            }
        });


    }
}