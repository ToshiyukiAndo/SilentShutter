package mylibs;

import android.graphics.Bitmap;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by toshiyukiando on 2015/12/04.
 */
public class libs {

    static public void saveBitmapToSd(Bitmap mBitmap) {
        try {
            // sdcardフォルダを指定
            String SDFile = android.os.Environment.getExternalStorageDirectory().getPath();
//                    + "/Android/data/"+getPackageName();
            File root = new File(SDFile);

            // 日付でファイル名を作成

            // 保存処理開始
            FileOutputStream fos = null;
            fos = new FileOutputStream(new File(root,  "a.jpg"));

            // jpegで保存
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            // 保存処理終了
            fos.close();
        } catch (Exception e) {
            Log.e("Error", "" + e.toString());
        }
    }
}
