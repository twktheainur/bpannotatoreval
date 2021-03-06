package org.pratikpharma.cli;

import java.io.*;

/**
 *
 * @author Amine
 */
public final class ClefEHealth2017T1ResultFallbackRaw {

    private ClefEHealth2017T1ResultFallbackRaw() {
    }

    public static void main(final String[] args) throws FileNotFoundException, IOException {
        String PathMFC=args[0];
        String PathAll=args[1];
        BufferedReader rMFC = new BufferedReader(new InputStreamReader(new FileInputStream(PathMFC)));
        BufferedReader rAll = new BufferedReader(new InputStreamReader(new FileInputStream(PathAll)));
        PrintWriter Out = new PrintWriter(args[2]);
        String line,docID,lineID;
        String lineH="",docIDH="",lineIDH="";
        while ((line=rMFC.readLine())!=null){
            if (line.endsWith(";")){
                docID=line.split(";")[0];
                lineID=line.split(";")[2];
                while ((!docIDH.equals(docID) || !lineIDH.equals(lineID)) && (lineH=rAll.readLine())!=null){
                    //lineH=rHeur.readLine();
                    //System.out.println(lineH);
                    docIDH=lineH.split(";")[0];
                    lineIDH=lineH.split(";")[2];
                }
                Out.println(lineH);
            }
            else Out.println(line);
            Out.flush();
        }
        Out.close();
    }
    
}
