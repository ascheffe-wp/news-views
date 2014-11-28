package com.schef.rss.android;

import com.badlogic.gdx.math.Vector3;

import java.util.List;

/**
 * Created by scheffela on 11/28/14.
 */
public class RotateRow implements Runnable {

    public Ars3d ars3d;
    public float degreesToRotate;
    public int rotateCycles;
    public long cycleTime;
    public ArticleInstance ai;

    public RotateRow(Ars3d ars3d, ArticleInstance ai, float degreesToRotate, int rotateCycles, long cycleTime) {
        this.ars3d = ars3d;
        this.degreesToRotate = degreesToRotate;
        this.rotateCycles = rotateCycles;
        this.cycleTime = cycleTime;
        this.ai = ai;
    }

    @Override
    public void run() {
        try {
            Ars3d.tapable = false;
            List<ArticleInstance> row = ars3d.rows.get(ai.row);
            if(row != null) {
                for (int count = 0; count < rotateCycles; count++) {
                    try {
                        Thread.sleep(cycleTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for (ArticleInstance ai : row) {
                        ai.transform.rotate(0.0f, 600.0f, 0.0f, degreesToRotate);
                        ai.center.rotate(new Vector3(0.0f, 600.0f, 0.0f), degreesToRotate);
                        ai.dimensions.rotate(new Vector3(0.0f, 600.0f, 0.0f), degreesToRotate);
                    }
                }
                for (ArticleInstance ai2 : row) {
                    if (degreesToRotate > 0) {
                        float newRowId = ai2.col - 1;
                        if (newRowId < 0) {
                            newRowId = 8;
                        }
                        ars3d.cols.get(ai2.col).remove(ai2);
                        ai2.col = newRowId;
                        ars3d.cols.get(ai2.col).add(ai2);
                    } else {
                        float newRowId = ai2.col + 1;
                        if (newRowId > 8) {
                            newRowId = 0;
                        }
                        ars3d.cols.get(ai2.col).remove(ai2);
                        ai2.col = newRowId;
                        ars3d.cols.get(ai2.col).add(ai2);
                    }
//                        cols.get(ai2.col).set((int)ai.row,ai2);
                }

//                    resetColumns(ai.row);

            }
        } finally {
            Ars3d.tapable = true;
            Ars3d.resetBlending = true;
        }
    }


    private void resetColumns (float rowId) {
//            List<ArticleInstance> row0 = ars3d.rows.get(0f);
//            List<ArticleInstance> row1 = ars3d.rows.get(1f);
//            List<ArticleInstance> row2 = ars3d.rows.get(2f);
//
//            List<ArticleInstance> rowToProcess;
//            if(rowId == 0.0f) {
//                rowToProcess = row0;
//            } else if(rowId == 1.0f) {
//                rowToProcess = row1;
//            } else {
//                rowToProcess = row2;
//            }
//
//            for(int i = 0; i < rowToProcess.size(); i++) {
//
//                float colToCorrect = rowToProcess.get(i).col;
//
//                row0.get(i).col = colToCorrect;
//                row1.get(i).col = colToCorrect;
//                row2.get(i).col = colToCorrect;
//            }



        List<ArticleInstance> row0 = ars3d.rows.get(0f);
        List<ArticleInstance> row1 = ars3d.rows.get(1f);
        List<ArticleInstance> row2 = ars3d.rows.get(2f);
        for(int i = 0; i < row0.size(); i++) {
            float col0val = row0.get(i).col;
            float col1val = row1.get(i).col;
            float col2val = row2.get(i).col;

//                List<ArticleInstance> newCol = new ArrayList<ArticleInstance>();
            if(col0val == col1val) {
                row0.get(i).col = col2val;
                row1.get(i).col = col2val;
            } else if(col0val == col2val) {
                row0.get(i).col = col1val;
                row2.get(i).col = col1val;
            } else if(col1val == col2val) {
                row1.get(i).col = col0val;
                row2.get(i).col = col0val;
            }
//                newCol.add(row0.get(i));
//                newCol.add(row1.get(i));
//                newCol.add(row2.get(i));
//                cols.put(new Float(row0.get(i).col), newCol);

        }


    }
}
