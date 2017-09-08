package com.flb.imebraandroid;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.imebra.CodecFactory;
import com.imebra.DataSet;
import com.imebra.Image;
import com.imebra.LUT;
import com.imebra.ReadingDataHandlerNumeric;
import com.imebra.StreamReader;
import com.imebra.VOILUT;
import com.imebra.VOIs;
import com.imebra.drawBitmapType_t;
import com.imebra.imebra;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.imebra.drawBitmapType_t.drawBitmapRGBA;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn1).setOnClickListener(this);
        findViewById(R.id.btn2).setOnClickListener(this);
        findViewById(R.id.btn3).setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        Button btn = (Button) view;
        loadDICOM(btn.getText().toString());
    }


    public void loadDICOM(String name){
        String newPath = getCacheDir()+"/"+name;
        copyFilesFassets(this,name,newPath);

        DataSet dataSet = CodecFactory.load(newPath);
        Image image = dataSet.getImage(0);
        Log.d("height",""+image.getHeight());
        Log.d("width",""+image.getWidth());

        long height = image.getHeight();
        long width = image.getWidth();
        com.imebra.TransformsChain chain = new com.imebra.TransformsChain();

        if(com.imebra.ColorTransformsFactory.isMonochrome(image.getColorSpace()))
        {
            // Allocate a VOILUT transform. If the DataSet does not contain any pre-defined
            //  settings then we will find the optimal ones.
            VOILUT voilutTransform = new VOILUT();

            // Retrieve the VOIs (center/width pairs)
            VOIs vois = dataSet.getVOIs();

            // Retrieve the LUTs
            List<LUT> luts = new ArrayList<LUT>();
            for(long scanLUTs = 0; ; scanLUTs++)
            {
                try
                {
                    luts.add(dataSet.getLUT(new com.imebra.TagId(0x0028,0x3010), scanLUTs));
                }
                catch(Exception e)
                {
                    break;
                }
            }

            if(!vois.isEmpty())
            {
                voilutTransform.setCenterWidth(vois.get(0).getCenter(), vois.get(0).getWidth());
            }
            else if(!luts.isEmpty())
            {
                voilutTransform.setLUT(luts.get(0));
            }
            else
            {
                voilutTransform.applyOptimalVOI(image, 0, 0, width, height);
            }

            chain.addTransform(voilutTransform);
        }

        com.imebra.DrawBitmap draw = new com.imebra.DrawBitmap(chain);

// Ask for the size of the buffer (in bytes)
        long requestedBufferSize =width*height*4;

        byte buffer[] = new byte[(int)requestedBufferSize]; // Ideally you want to reuse this in subsequent calls to getBitmap()
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

// Now fill the buffer with the image data and create a bitmap from it
        draw.getBitmap(image, drawBitmapRGBA, 4, buffer);
        Bitmap renderBitmap = Bitmap.createBitmap((int)image.getWidth(), (int)image.getHeight(), Bitmap.Config.ARGB_8888);
        renderBitmap.copyPixelsFromBuffer(byteBuffer);


        ImageView imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageBitmap(renderBitmap);
    }

    public void copyFilesFassets(Context context, String oldPath, String newPath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath);//获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {//如果是目录
                File file = new File(newPath);
                file.mkdirs();//如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyFilesFassets(context,oldPath + "/" + fileName,newPath+"/"+fileName);
                }
            } else {//如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount=0;
                while((byteCount=is.read(buffer))!=-1) {//循环从输入流读取 buffer字节
                    fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
                }
                fos.flush();//刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
    }


}
