// src/model/BoardTools.java
package lightbulb.model;

import java.util.*;

public final class BoardTools {
    /** true if (r,c) is connected to the source at current turns */
    public static boolean[][] computePowered(Board b){
        int R=b.getRows(), C=b.getCols();
        boolean[][] powered = new boolean[R][C];

        int srcR=-1,srcC=-1;
        for(int r=0;r<R;r++)
            for(int c=0;c<C;c++)
                if(b.getCell(r,c).getElement() instanceof PowerSource){
                    srcR=r; srcC=c;
                }
        if(srcR==-1) return powered;

        Deque<int[]> q = new ArrayDeque<>();
        powered[srcR][srcC]=true; q.add(new int[]{srcR,srcC});

        while(!q.isEmpty()){
            int[] p=q.poll(); int r=p[0],c=p[1];
            Element e=b.getCell(r,c).getElement();
            if(e==null) continue;

            if(e instanceof Bulb bulb){
                continue;
            }

            for(Direction d:e.getConnections()){
                int nr=r+d.dRow(), nc=c+d.dCol();
                if(nr<0||nr>=R||nc<0||nc>=C) continue;
                Element n = b.getCell(nr,nc).getElement();
                if(n==null) continue;

                if(!(n instanceof Bulb)){
                    if(!n.getConnections().contains(d.opposite())) continue;
                }
                else {
                    Direction lead = ((Bulb)n).getLead();
                    if(lead != d.opposite()) continue;
                }

                if(!powered[nr][nc]){
                    powered[nr][nc]=true;
                    if(!(n instanceof Bulb))
                        q.add(new int[]{nr,nc});
                }
            }
        }
        return powered;
    }
    private BoardTools(){}
}
