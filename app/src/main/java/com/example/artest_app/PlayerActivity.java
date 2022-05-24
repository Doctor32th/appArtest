package com.example.artest_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.gauravk.audiovisualizer.visualizer.BlastVisualizer;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    //Declaración de los botones que componen el reproductor de música
    Button btnPlay, btnNext, btnPrevious, btnFastForward, btnFastBackward;

    //Declaración de los textos relativos a la canción, que marcarán su nombre, comienzo y fin
    TextView txtSongName, txtSongStart, txtSongEnd;

    //Declaración de la barra de progreso de la canción
    SeekBar seekMusicBar;

    //Declaración de la variable. Definirá los efectos visuales en el reproductor
    BarVisualizer barVisualizer;

    //Declaración de la imageView (icono de la nota musical del reproductor)
    ImageView imageView;

    //Declaración de la variable de tipo String songName
    String songName;

    //Inicialización de la constante del nombre de la canción
    public static final String EXTRA_NAME = "song_name";

    //Declaración de la variable estática de la clase mediaplayer
    static MediaPlayer mediaPlayer;

    //Variable que determina la posición que tendrá cada canción dentro del array mySongs
    int position;

    //Declaración del array de canciones
    ArrayList<File> mySongs;

    //Declaración de la variable actualizar SeekBar mediante el uso de un hilo o thread
    Thread updateSeekBar;

    /**
     * Esta función activa el botón para retroceder a la biblioteca de canciones
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        //Colocación del nombre de la aplicación
        getSupportActionBar().setTitle("Artest");
        //Activación del botón para retroceder
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //Colocación del icono de la aplicación
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setIcon(R.drawable.logo);

        //Inicialización de los botones sincronizándolos con su vista en el activity_player.xml
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnPlay = findViewById(R.id.btnPlay);
        btnFastForward = findViewById(R.id.btnFastForward);
        btnFastBackward = findViewById(R.id.btnFastBackward);

        //Inicialización de los textos visibles en el reproductor
        txtSongName = findViewById(R.id.txtSong);
        txtSongStart = findViewById(R.id.txtSongStart);
        txtSongEnd = findViewById(R.id.txtSongEnd);

        //Inicialización de la barra de progreso sincronizándola con su vista en el xml
        seekMusicBar = findViewById(R.id.seekBar);

        //Inicialización del efecto visual sincronizándolo con su vista en el xml
        barVisualizer = findViewById(R.id.green_wave);

        //Inicialización del imageView sincronizándolo con su vista en el xml
        //Se trata de la imagen de la nota
        imageView = findViewById(R.id.imgView);

        //Estructura if en la cual si el mediaPlayer es diferente de null comenzará y podrá pausar
        if (mediaPlayer != null){
            mediaPlayer.start();
            mediaPlayer.release();
        }

        //Declaración e inicialización del intent que nos dará el intent
        Intent intent = getIntent();

        //Obtención de los extras a través del bundle
        Bundle bundle = intent.getExtras();

        //El método adjunto al bundle nos devolverá a la biblioteca de canciones gracias al intent
        mySongs = (ArrayList)bundle.getParcelableArrayList("songs");

        //Esta variable representa al nombre de la canción que se está reproduciendo
        String sName = intent.getStringExtra("songname");

        //Esta variable representa a la posición de la canción que se está reproduciendo
        position = bundle.getInt("pos", 0);

        //txtSongName se hará visible mientras se reproduce el contenido auditivo que lo representa
        txtSongName.setSelected(true);

        //La uri convertirá el listado de canciones en un string para que podamos escucharlas
        Uri uri = Uri.parse(mySongs.get(position).toString());
        songName = mySongs.get(position).getName();
        txtSongName.setText(songName);

        //mediaplayer creará el contexto gracias al uri y seguidamente la canción comenzará a sonar
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        /**
         * Este nuevo hilo sobreescribirá la función run
         * Dicha función tratará la duración total de la canción y la posición actual del seekBar
         * Mediante una estructura try se obtendrá la posición actual de la barra de progreso
         * y esto nos permitirá como se mueve a medida que la canción transcurre
         */
        updateSeekBar = new Thread(){
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;

                while(currentPosition < totalDuration) {

                    try {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekMusicBar.setProgress(currentPosition);

                    } catch (InterruptedException | IllegalStateException e) {
                        e.printStackTrace();
                    }
                }

            }
        };

        //Este método nos dará la duración máxima en función de la canción que esté sonando
        seekMusicBar.setMax(mediaPlayer.getDuration());
        //Comienzo de la actualización de la barra de progreso
        updateSeekBar.start();
        //Estas líneas dibujarán el movimiento de la seekBar con un color celeste
        seekMusicBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.MULTIPLY);
        seekMusicBar.getThumb().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);

        /**
         * setOnSeekBarChangeListener permitirá el libre movimiento de la seekBar para que
         * podamos seleccionar un determinado fragmento de la canción
         */
        seekMusicBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        //Esta variable creará el tiempo y nos dará la duración final de la canción
        //De esta manera nos devuelve el tiempo en formato textView
        String endTime = createTime(mediaPlayer.getDuration());
        txtSongEnd.setText(endTime);

        //Gracias al objeto Handler y a la variable delay se nos mostrará como se modifica el tiempo
        //Que lleva de progreso la canción cuando movemos la seekBar o pulsamos
        //Sobre los botones que nos permiten avanzar o ir atrás sobre la canción
        final Handler handler = new Handler();
        final int delay = 1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                txtSongStart.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        }, delay);

        /**
         * Este método onClick hará que cuando clickemos sobre el botón play la canción se pause
         * o se reanude en función de la siguiente estructura if
         * La estructura if dará funcionalidad a los botones play y pause según se de el caso
         */
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Si la canción está sonando y la pausamos, aparecerá el botón play
                //El botón play es la señal que nos permitirá reanudar la canción por donde iba
                if(mediaPlayer.isPlaying()){
                    btnPlay.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                } else {
                    //Si la canción está en pausa y la reanudamos, aparecerá el botón pause
                    //El botón pause es la señal que nos permitirá pausar la canción cuando queramos
                    btnPlay.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();

                    /**
                     * Después de hacer click sobre la canción y cambiar a la pantalla del
                     * reproductor de música, el imageView (la imagen de la nota musical)
                     * generará una animación rotando en círculo
                     */
                    TranslateAnimation moveAnim = new TranslateAnimation(-25, 25,-25, 25);
                    moveAnim.setInterpolator(new AccelerateInterpolator());
                    moveAnim.setDuration(600);
                    moveAnim.setFillEnabled(true);
                    moveAnim.setFillAfter(true);
                    moveAnim.setRepeatMode(Animation.REVERSE);
                    moveAnim.setRepeatCount(1);

                    //Aquí estamos transfiriendo la animación al imageView
                    imageView.startAnimation(moveAnim);

                }
            }
        });

        /**
         *
         */
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnNext.performClick();
            }
        });

        //Estas tres líneas se encargan de mostrar el efecto visual en la parte inferior
        //del reproductor de música cuando esta sonando la canción
        int audioSessionId = mediaPlayer.getAudioSessionId();
        if (audioSessionId != -1) {
            barVisualizer.setAudioSessionId(audioSessionId);
        }

        /**
         * Método aplicado sobre el botón next.
         * Consistirá en la rotación de 360 grados del imageView tras pulsar el botón next
         * Además al pulsar sobre este, el reproductor pausará la canción actual y reanudará
         * la siguiente
         */
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mediaPlayer.stop();
                mediaPlayer.release();

                //position pasa a la siguiente canción de la lista dentro del array mySongs
                position = ((position + 1)%mySongs.size());

                //Uri convertirá el listado de canciones en un string para que podamos escucharlas
                Uri uri = Uri.parse(mySongs.get(position).toString());

                //Las siguientes líneas cambiarán el contexto del reproductor mostrando
                //de manera visual el nombre de la siguiente pista de audio
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                songName = mySongs.get(position).getName();
                txtSongName.setText(songName);
                mediaPlayer.start();

                startAnimation(imageView, 360f);
            }
        });

        /**
         * Método aplicado sobre el botón previous.
         * Consistirá en la rotación de -360 grados del imageView tras pulsar el botón previous
         * Además al pulsar sobre este, el reproductor pausará la canción actual y reanudará
         * la anterior
         */
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mediaPlayer.stop();
                mediaPlayer.release();

                //position pasa a la canción anterior de la lista dentro del array mySongs
                position = ((position - 1) < 0)?(mySongs.size() -1):position-1;
                Uri uri = Uri.parse(mySongs.get(position).toString());

                //Las siguientes líneas cambiarán el contexto del reproductor mostrando
                //de manera visual el nombre de la siguiente pista de audio
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                songName = mySongs.get(position).getName();
                txtSongName.setText(songName);
                mediaPlayer.start();

                startAnimation(imageView, -360f);
            }
        });

        //El método setOnclickListener para este botón permitirá que al pulsar sobre este
        //podamos avanzar de manera rápida la canción que estemos escuchando
        btnFastForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 10000);
                }
            }
        });

        //El método setOnclickListener para este botón permitirá que al pulsar sobre este
        //podamos retroceder de manera rápida la canción que estemos escuchando
        btnFastBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 10000);
                }
            }
        });

    }

    /**
     * Esta función nos permitirá mover el imageview de manera tactil a nuestro antojo
     * @param view
     * @param degree
     */
    public void startAnimation(View view, Float degree){
        //Declaración e inicialización de un objeto de la clase ObjectAnimator
        //Determinará el grado de rotación que adoptará el imageView en grados usando float
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, degree);

        //Determinación de la duración de la animación de la nota musical (la imageView)
        objectAnimator.setDuration(1000);

        //Declaración de un objeto de la clase AnimatorSet. Este dará inicio a la animación
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator);
        animatorSet.start();
    }

    /**
     * Está función nos dará el tiempo de la canción según dónde movamos la seekBar.
     * @param duration
     * @return
     */
    public String createTime(int duration){
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        //Formato que va a adquirir el tiempo cuando movamos la seekBar
        time = time + min + ":";

        if (sec < 10) {
            time+="0";
        }

        //Esta línea hará que la duración de la seekBar coja el formato adecuado
        //Para que así marque los segundos que dura la canción según sobre cuál pulsemos
        time+=sec;
        return time;
    }
}