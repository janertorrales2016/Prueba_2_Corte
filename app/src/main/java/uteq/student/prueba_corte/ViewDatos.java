package uteq.student.prueba_corte;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import uteq.student.prueba_corte.WebService.Asynchtask;
import uteq.student.prueba_corte.WebService.WebService;

public class ViewDatos extends AppCompatActivity implements Asynchtask, OnMapReadyCallback {
    private  String URL = "http://www.geognos.com/api/en/countries/info/";
    private final String URLPNG ="http://www.geognos.com/api/en/countries/flag/";
    private TextView txtdata,txtpa;
    private ImageView image;
    private GoogleMap mMap;
    private  Double West,East,North,South,latitud ,longitud ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_datos);
        //asignacion de variables
        txtdata = findViewById(R.id.txtdatos);
        txtpa = findViewById(R.id.txtpais);
        image = findViewById(R.id.imageView);
        //obtencion de parametros del MainActivity
        Bundle b= this.getIntent().getExtras();
        //cargar png de bandera
        Glide.with(this).load(URLPNG+b.getString("PAIS")+".png").into(image);
        //obtener data del pais
        URL=URL+b.getString("PAIS")+".json";
        Map<String, String> datos = new HashMap<String, String>();
        WebService ws = new WebService(URL,datos,ViewDatos.this,ViewDatos.this);
        ws.execute("GET");
    }

    @Override
    public void processFinish(String result) throws JSONException {
        try {
            String valor=" ";
            JSONObject JSONobjet =  new JSONObject(result);
            //nombre del pais
            JSONObject resul= new JSONObject(JSONobjet.getString("Results"));
            txtpa.setText(resul.getString("Name"));
            //nombre capital del pais
            JSONObject capital=new JSONObject(resul.getString("Capital"));
            valor = "Capital: \t\t\t"+capital.getString("Name")+"\n";
            //obtencion de los otros datos
            JSONObject countryCodes=new JSONObject(resul.getString("CountryCodes"));
            valor= valor + "Code ISO 2: \t\t\t"+countryCodes.getString("iso2")+"\n";
            valor= valor + "Code ISO Num: \t\t\t"+countryCodes.getString("isoN")+"\n";
            valor= valor + "Code ISO 3: \t\t\t"+countryCodes.getString("iso3")+"\n";
            valor= valor + "Code ISO FIPS: \t\t\t"+countryCodes.getString("fips")+"\n";
            valor= valor + "Tel Prefix: \t\t\t"+resul.getString("TelPref")+"\n";
            valor= valor + "Cneter: \t\t\t"+resul.getString("GeoPt")+"\n";
            txtdata.setText(valor);
            //Coordenadas para el GeoRectangle
            JSONObject geoRectangle=new JSONObject(resul.getString("GeoRectangle"));
            West=Double.parseDouble(geoRectangle.getString("West"));
            East=Double.parseDouble(geoRectangle.getString("East"));
            North=Double.parseDouble(geoRectangle.getString("North"));
            South=Double.parseDouble(geoRectangle.getString("South"));
           //GeoPt para modificar ubicacion de camara
            JSONArray geopt=resul.getJSONArray("GeoPt");
            latitud  = geopt.getDouble(0);
            longitud = geopt.getDouble(1);

            SupportMapFragment mapFragmnt = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragmnt.getMapAsync(this);

        }catch(JSONException e){
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap= googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //creacion del GeoRectangle
        PolylineOptions poligono = new PolylineOptions()
                .add(new LatLng(North, West))
                .add(new LatLng(North, East))
                .add(new LatLng(South, East))
                .add(new LatLng(South, West))
                .add(new LatLng(North, West));
        mMap.addPolyline(poligono);
        //movimiento de la camara
        CameraUpdate camara = CameraUpdateFactory.newLatLngZoom(new LatLng(latitud ,longitud ), 3);
        mMap.moveCamera(camara);
    }
}