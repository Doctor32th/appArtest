package com.example.artest_app;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView listView; //Declaración de la variable de tipo ListView
    String[] items; //Declaración del array de tipo String con los items
    //Los items en este caso son los nombres de cada canción

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Identificación de la variable listView ya declarada con el xml del activity main
        listView = (ListView) findViewById(R.id.listView);

        //Llamando a este método en el onCreate la aplicación pedirá permisos al móvil
        //para acceder a las canciones
        runtimePermission();
    }

    /**
     * Estos métodos procedentes de la librería Dexter son los que regularán los permisos
     * de acceso al dispositivo. Podemos ver que si permitimos que la aplicación acceda
     * al dispositivo llamará al método displaySongs()
     */
    public void runtimePermission() {
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        displaySong();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    /**
     * Función de tipo ArrayList que devolverá un arreglo con el listado de canciones
     * @param file
     * @return
     */
    public ArrayList<File> findSong(File file) {
        ArrayList<File> arrayList = new ArrayList<>(); //Arraylist de todas la canciones

        File f = file; //Objeto de tipo File

        File[] files = null; //ArrayList de tipo File

        /**
         * Estructura try/catch. Es muy importante para atrapar el error
         * de tipo NullpointerException
         */
        try {

            files = f.listFiles(); //La variable de tipo File va a listar todos los archivos con listFiles()

            //Si la longitud de los archivos es mayor que 0
            if (files.length > 0) {

                //Estructura for en la cual se itera cada archivo
                for (File singleFile : files) {

                    //Si la canción está en el directorio o se encuentra oculta
                    if (singleFile.isDirectory() && !singleFile.isHidden()) {

                        arrayList.addAll(findSong(singleFile)); //Se añaden todas las canciones encontradas
                    } else {

                        //Si el nombre de la canción acaba en .mp3 o .wav
                        if (singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav")) {
                            arrayList.add(singleFile); //Se añadirá la canción al array
                        }
                    }
                }
            }/*else {
                //Este objeto de tipo Path obtendrá la ruta de los archivos en el móvil
                Path path = Paths.get("/storage/emulated/0/Music");
                File filePath = path.toFile();
                arrayList.add(filePath); //Cada archivo de tipo File se añadirá al arreglo
            }*/

        } catch (NullPointerException e) {
            System.out.println("Error:" + e + "" + e.getStackTrace()[0].getLineNumber());
        }
        return arrayList; //Este método nos devolverá un arreglo con todas las canciones
    }

    /**
     * Función que mostrará las canciones del directorio del móvil en la aplicación
     * tras autorizar los permisos de configuración
     */
    public void displaySong() {
        //Constante final mySongs de tipo arrayList con el listado de canciones
        //Con findSong obtendremos las canciones del almacenamiento externo del móvil
        final ArrayList<File> mySongs = findSong(Environment.getExternalStorageDirectory());

        //Esta variable se inicializó en las primeras líneas
        //Se inicializa como un nuevo string mostrando el tamaño de las canciones
        items = new String[mySongs.size()];

        //Estructura for que iterará cada una de las canciones
        for (int i = 0; i < mySongs.size(); i++) {

            //La variable items obtendrá el nombre de cada archivo de audio y lo reemplazará por un espacio vacio
            //De esta manera podrá introducirse cada canción en el array de strings sin mostrar su formato
            //Esto le dará a la biblioteca de canciones un aspecto más elegante y menos cargado
            items[i] = mySongs.get(i).getName().toString().replace(".mp3", "").replace(".wav", "");
        }

        //Objeto de tipo customadapter
        customAdapter customAdapter = new customAdapter();
        listView.setAdapter(customAdapter);

        //El siguiente método hará que al hacer click sobre una canción nos lleve a otra activity
        /**
         * La función onItemClick nos permitirá acceder a la Player Activity (al reproductor de
         * música) cuando estando en la biblioteca de canciones pulsamos sobre una para
         * escucharla.
         */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String songName = (String) listView.getItemAtPosition(position);

                //Aquí se crea un intent para poder acceder a la playeractivity
                //Haciendo click sobre el array de archivos mySongs
                startActivity(new Intent(getApplicationContext(), PlayerActivity.class)
                        .putExtra("songs", mySongs)
                        .putExtra("songname", songName)
                        .putExtra("pos", position)
                );
            }
        });

    }

    /**
     * Este método que hereda de BaseAdapter será aquel que cargará el contenido visual de la aplicación
     * Esta herencia hace que se sobreescriban estos cuatro métodos que vemos a continuación
     * Los métodos getItem y getItemId no son necesarios en la aplicación. Es por ello que no los
     * vamos a implementar.
     */
    class customAdapter extends BaseAdapter {

        /**
         * Obtendrá la longitud de todos los items
         * @return items.length
         */
        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        /**
         * Este método es muy importante, ya que nos devolverá la vista de la lista de
         * canciones inflando el layout.
         * @param position
         * @param convertView
         * @param parent
         * @return view
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.list_item, null);
            TextView txtSong = view.findViewById(R.id.txtSong);
            txtSong.setSelected(true);
            txtSong.setText(items[position]);
            return view;
        }
    }

}