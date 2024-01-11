package com.example.fuelefficiencypredictor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    ScrollView sv;
    Interpreter interpreter;
    float[] mean =  {23.310510f,	5.477707f,	195.318471f,	104.869427f,	2990.251592f,	15.559236f,	75.898089f,	0.624204f,	0.178344f,	0.197452f};
    float[] std = {7.728652f,	1.699788f,	104.331589f,	38.096214f,	843.898596f,	2.789230f,	3.675642f,	0.485101f,	0.383413f,	0.398712f };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            interpreter = new Interpreter(loadModelFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sv = (ScrollView)findViewById(R.id.sv);
        final EditText cylinders = findViewById(R.id.editText);
        final EditText displacement = findViewById(R.id.editText2);
        final EditText horsePower = findViewById(R.id.editText3);
        final EditText weight = findViewById(R.id.editText4);
        final EditText accelration = findViewById(R.id.editText5);
        final EditText modelYear = findViewById(R.id.editText6);
        final Spinner origin = findViewById(R.id.spinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_spinner_dropdown_item,new String[]{"USA","Europe","Japan"});
        origin.setAdapter(arrayAdapter);
        final TextView result = findViewById(R.id.textView2);

        Button btn = findViewById(R.id.button2);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sv.scrollTo( sv.getBottom(),0);
                float cylindersVal = Float.parseFloat(cylinders.getText().toString());
                float displacementVal = Float.parseFloat(displacement.getText().toString());
                float horsePowerVal = Float.parseFloat(horsePower.getText().toString());
                float weightVal = Float.parseFloat(weight.getText().toString());
                float accelrationVal = Float.parseFloat(accelration.getText().toString());
                float modelYearVal = Float.parseFloat(modelYear.getText().toString());
                float originA = 0;
                float originB = 0;
                float originC = 0;
                switch (origin.getSelectedItemPosition())
                {
                    case 0:
                        originA = 1;
                        originB = 0;
                        originC = 0;
                        break;
                    case 1:
                        originA = 0;
                        originB = 1;
                        originC = 0;
                        break;
                    case 2:
                        originA = 0;
                        originB = 0;
                        originC = 1;
                        break;
                }
                cylindersVal = (cylindersVal - mean[0]/std[0]);
                displacementVal= (displacementVal - mean[1]/std[1]);
                horsePowerVal= (horsePowerVal - mean[2]/std[2]);
                weightVal = (weightVal - mean[3]/std[3]);
                accelrationVal= (accelrationVal - mean[4]/std[4]);
                modelYearVal= (modelYearVal - mean[5]/std[5]);
                originA = (originA - mean[6]/std[6]);
                originB= (originB - mean[7]/std[7]);
                originC = (originC - mean[8]/std[8]);


                float[][] input = new float[1][9];
                input[0][0] = cylindersVal;
                input[0][1] = displacementVal;
                input[0][2] = horsePowerVal;
                input[0][3] =weightVal;
                input[0][4] = accelrationVal;
                input[0][5] = modelYearVal;
                input[0][6] = originA;
                input[0][7] = originB;
                input[0][8] = originC;

              float  output =  doInference(input);
                result.setText(output+ "MPG");
            }
        });
    }

    //TODO pass input to model and get output
    public float doInference(float[][] input)
    {
        float[][] output = new float[1][1];

        interpreter.run(input,output);

        return output[0][0];
    }

    //TODO load tflite model
    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor assetFileDescriptor = this.getAssets().openFd("automobile.tflite");
        FileInputStream fileInputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = assetFileDescriptor.getStartOffset();
        long length = assetFileDescriptor.getLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, length);
    }
}
