package br.org.catolicasc.jogopokemon;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ImageView imageView;
    private TextView Acertos;
    private TextView Erros;
    private TextView Score;
    private Button btnOption1;
    private Button btnOption2;
    private Button btnOption3;
    private Button btnOption4;
    private String vGlobal;
    private AlertDialog alerta;
    private AlertDialog.Builder builder;
    private AlertDialog.Builder confirmation;
    private int numeroJogadas;
    private TextView txvCounter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnOption4 = findViewById(R.id.btnOption4);
        Acertos = findViewById(R.id.txtPontosAcertos);
        Erros = findViewById(R.id.txtPontosErros);
        Score = findViewById(R.id.txtScoreValue);
        builder = new AlertDialog.Builder(this);
        confirmation = new AlertDialog.Builder(this);
        numeroJogadas = 1;
        txvCounter = findViewById(R.id.txvCounter);

        CallTimer();

        final DownloadDeDados downloadDeDados = new DownloadDeDados();
        downloadDeDados.execute("https://raw.githubusercontent.com/Biuni/PokemonGO-Pokedex/master/pokedex.json");

        View.OnClickListener listenerButtons = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                String nome = b.getText().toString();
                if(numeroJogadas < 11) {
                    if (vGlobal.equals(nome)) {
                        int AcertoAtual = Integer.parseInt(Acertos.getText().toString());
                        AcertoAtual += 1;
                        Acertos.setText(String.valueOf(AcertoAtual));

                        int ScoreAtual = Integer.parseInt(Score.getText().toString());
                        ScoreAtual += 100;
                        Score.setText(String.valueOf(ScoreAtual));

                        builder.setTitle(nome + " diz:");
                        builder.setMessage("VOCÊ ACERTOU MEU NOME!!!");
                        alerta = builder.create();
                        alerta.show();

                        new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                alerta.cancel();
                            }
                        },
                        1000);

                    } else {
                        int ErroAtual = Integer.parseInt(Erros.getText().toString());
                        ErroAtual += 1;
                        Erros.setText(String.valueOf(ErroAtual));

                        int ScoreAtual = Integer.parseInt(Score.getText().toString());
                        ScoreAtual -= 100;
                        Score.setText(String.valueOf(ScoreAtual));

                        builder.setTitle("Pokemon diz:");
                        builder.setMessage("VOCÊ ERROU!!! MEU NOME É: " + vGlobal);
                        alerta = builder.create();
                        alerta.show();

                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        alerta.cancel();
                                    }
                                },
                                1000);
                    }
                    if(numeroJogadas == 10) {
                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        FimDoJogo();
                                    }
                                },
                        2500);
                    }
                    else {
                        numeroJogadas++;
                        final DownloadDeDados downloadDeDados = new DownloadDeDados();
                        downloadDeDados.execute("https://raw.githubusercontent.com/Biuni/PokemonGO-Pokedex/master/pokedex.json");
                    }
                }
            }
        };
        btnOption1.setOnClickListener(listenerButtons);
        btnOption2.setOnClickListener(listenerButtons);
        btnOption3.setOnClickListener(listenerButtons);
        btnOption4.setOnClickListener(listenerButtons);
    }

    private void CallTimer(){
        new CountDownTimer(60000, 1000) {
            public void onTick(long millisecondsUntilDone) {
                txvCounter.setText(String.valueOf(millisecondsUntilDone / 1000));
            }

            public void onFinish() {
                FimDoJogo();
                Log.i("Done!", "Coundown Timer Finished");
            }
        }.start();
    }

    private void FimDoJogo(){
        confirmation.setTitle("FIM DO JOGO!!! Sua pontuação foi: " + Score.getText().toString() +
                "   Acertos: "+Acertos.getText().toString() +
                "   Erros: "+Erros.getText().toString())
        .setMessage("Você deseja recomeçar?")
        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                numeroJogadas = 1;
                Score.setText("0");
                Acertos.setText("0");
                Erros.setText("0");
                dialog.cancel();
                final DownloadDeDados downloadDeDados = new DownloadDeDados();
                downloadDeDados.execute("https://raw.githubusercontent.com/Biuni/PokemonGO-Pokedex/master/pokedex.json");
                CallTimer();
                dialog.dismiss();
            }
        })


        .setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
                dialog.dismiss();
            }
        })
        .create().show();
    }



    private class DownloadDeDados extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: começa com o parâmetro: " + strings[0]);
            String jsonFeed = downloadJson(strings[0]);
            if (jsonFeed == null) {
                Log.e(TAG, "doInBackground: Erro baixando JSON");
            }
            return jsonFeed;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: parâmetro é: " + s);
            JSONTokener jsonTokener = new JSONTokener(s);
            try {
                SetJSONData(jsonTokener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void SetJSONData(JSONTokener jsonTokener){
            try {
                JSONObject json = new JSONObject(jsonTokener);
                JSONArray jsonArray = json.getJSONArray("pokemon");
                Random random = new Random();
                int[] indice = new int[4];
                for(int i =0; i<4;i++){
                    indice[i] = random.nextInt(100);
                }

                SetTextInButtons(random, jsonArray, indice);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void SetTextInButtons(Random random, JSONArray jsonArray, int[] indice){
            try {
                btnOption1.setText(jsonArray.getJSONObject(indice[0]).getString("name"));
                btnOption2.setText(jsonArray.getJSONObject(indice[1]).getString("name"));
                btnOption3.setText(jsonArray.getJSONObject(indice[2]).getString("name"));
                btnOption4.setText(jsonArray.getJSONObject(indice[3]).getString("name"));
                ImageDownloader imageDownloader = new ImageDownloader();
                int pokemon = indice[random.nextInt(4)];
                vGlobal = jsonArray.getJSONObject(pokemon).getString("name");
                Bitmap imagem = imageDownloader.execute(jsonArray.getJSONObject(pokemon).getString("img").replace("http", "https")).get();
                imageView.setImageBitmap(imagem);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }


        private String downloadJson(String urlString) {
            StringBuilder json = new StringBuilder();
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int resposta = connection.getResponseCode();
                Log.d(TAG, "downloadJson: O código de resposta foi: " + resposta);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));

                int charsLidos;
                char[] inputBuffer = new char[99999999];
                while (true) {
                    charsLidos = reader.read(inputBuffer);
                    if (charsLidos < 0) {
                        break;
                    }
                    if (charsLidos > 0) {
                        json.append(
                                String.copyValueOf(inputBuffer, 0, charsLidos));
                    }
                }
                reader.close();
                return json.toString();

            } catch (MalformedURLException e) {
                Log.e(TAG, "DownloadJSON: URL é inválida " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "downloadJson: Ocorreu um erro de IO ao baixar os dados: "
                        + e.getMessage());
            }
            return null;
        }
    }
    private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        private static final String TAG = "ImageDownloader";

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                Log.d(TAG, "doInBackground: A imagem foi baixada com sucesso!"+ url);

                return bitmap;
            } catch (Exception e) {
                Log.e(TAG, "doInBackground: Erro ao baixar imagem " + e.getMessage());
            }
            return null;
        }
    }

}
