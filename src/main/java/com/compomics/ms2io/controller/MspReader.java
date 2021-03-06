package com.compomics.ms2io.controller;

import com.compomics.ms2io.model.Spectrum;
import com.compomics.ms2io.model.Modification;
import com.compomics.ms2io.model.Peak;
import com.compomics.ms2io.model.IndexKey;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Genet
 */
public class MspReader extends SpectraReader {

    private File indexFile = null;
    List<IndexKey> IKey = null;

    /**
     * Constructor to create object for spectrum reader
     *
     * @param specFfile the spectrum file to be read
     */
    public MspReader(File specFfile) {
        super(specFfile);

    }

    /**
     * Constructor to create object for spectrum reader
     *
     * @param specFile the spectrum file to be read
     * @param indxFile index file that contains spectrum positions of the
     * spectrum file
     */
    public MspReader(File specFile, File indxFile) {
        super(specFile);
        this.indexFile = indxFile;

    }

    /**
     * Constructor to create object for spectrum reader
     *
     * @param specFile the spectrum file to be read
     * @param indK index list of the spectra to be read
     *
     */
    public MspReader(File specFile, List<IndexKey> indK) {
        super(specFile);
        this.IKey = indK;

    }

    /**
     * Read the whole spectrum from the given spectrum file
     *
     * @return List of spectrum
     */
    @Override
    public ArrayList<Spectrum> readAll() {

        ArrayList<Spectrum> spectra = new ArrayList<>();
        try {

            //BufferedReader br = new BufferedReader(new FileReader(this.spectraFile));
            BufferedRAF braf = new BufferedRAF(this.spectraFile, "r");
            IndexKey k = new IndexKey();
            Peak pk;
            ArrayList<Peak> pkList = new ArrayList<>();
            Spectrum spec = new Spectrum();
            String sequence="";

            String line = braf.readLine();
            while (line != null) {

                if (line.endsWith("\r")) {
                    line = line.replace("\r", "");
                }

                if (line.equals("")) {
                    spec.setNumPeaks(pkList.size());
                    k.setPM(spec.getPCMass());
                    k.setScanNum(spec.getScanNumber());
                    spec.setIndex(k);
                    spec.setPeakList(pkList);
                    spectra.add(spec);

                } else if (Character.isDigit(line.charAt(0))) {
                    String fline = line.replaceAll("\\s+", " ");
                    String[] p = fline.split(" ");
                    double pcm = Double.parseDouble(p[0]);
                    double pci = Double.parseDouble(p[1]);
                    String annotation = "";
                    if(p.length > 2){
                        annotation = p[2];
                    }
                    pk=new Peak(pcm, pci, annotation);                
                    pkList.add(pk);

                } else if (line.startsWith("Name")) {
                    pkList = new ArrayList<>();
                    spec = new Spectrum();
                    spec.setTitle(line.substring(line.indexOf(":") + 2));

                    sequence = line.substring(line.indexOf(":") + 2, line.indexOf("/"));
                    spec.setSequence(sequence);

                    
                    String ch = line.substring(line.indexOf('/') + 1);
                    spec.setCharge(ch);
                    ch = line.substring(line.indexOf(':') + 1, line.indexOf('/'));
                    spec.setSequence(ch);
                    k.setPos(braf.getFilePointer());
                    
                    String z= line.substring(line.indexOf('/')+1);
                    spec.setCharge(z);

                } else if (line.startsWith("MW:")) {
                    try {
                        double molecularWeight = Double.parseDouble(line.substring(line.indexOf(':') + 2));
                        spec.setMW(molecularWeight);
                    } catch (Exception e) {
                        System.out.println("An exception was thrown when trying to decode the msp Molecular Weight");

                    }
                } else if (line.startsWith("Comment")) {                   
                    spec.setComment(line);
                    String[] temp = line.split("\\s+");
                    int tempLen = temp.length;
                    List<Modification> mods = getModifications(line, sequence);
                    spec.setModification(mods);                  
                    
                    for (int b = 0; b < tempLen; b++) {
                        if (temp[b].startsWith("Parent=")) {
                            int indx=temp[b].indexOf("=") + 1;
                            double precmass = Double.parseDouble(temp[b].substring(indx));
                            spec.setPCMass(precmass);
                        } else if (temp[b].startsWith("Scan=")) {
                            int indx=temp[b].indexOf("=") + 1;
                            String scan = temp[b].substring(indx);
                            spec.setScanNumber(scan);
                        } else if (temp[b].startsWith("Protein=")) {
                            
                            int indx=temp[b].indexOf("=") +1;                            
                            String prot = temp[b].substring(indx);
                            spec.setProtein(prot);
                            
                        } 
                    }

                }
                line = braf.readLine();

            }

        } catch (IOException ex) {
            Logger.getLogger(MgfReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return spectra;

    }

    @Override
    public ArrayList<Spectrum> readPart(double precMass, double error) {

        ArrayList<Spectrum> selectedSpectra = new ArrayList<>(100000);
        List<Long> pos;

        Indexer indxer = new Indexer();
        try {
            if (this.IKey == null) {
                if (this.indexFile != null) {
                    this.IKey = indxer.readFromFile(indexFile);
                } else {
                    
                    //Index key not found, it should be read from file or should be provided
                    
                }
            }
            pos = positionsToberead(this.IKey, precMass, error);
            int len = pos.size();
            for (int a = 0; a < len; a++) {
                selectedSpectra.add(readAt(pos.get(a)));
            }
        } catch (IOException ex) {
            Logger.getLogger(MgfReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return selectedSpectra;
    }

    /**
     * Reads spectrum at the specified position
     *
     * @param position position to be read
     * @return spectrum at the location position
     */
    @Override
    public Spectrum readAt(Long position) {

        Spectrum spec = new Spectrum();
        try {

            String line;
            Peak pk;
            ArrayList<Peak> pkList;

            BufferedRAF braf = new BufferedRAF(this.spectraFile, "r");
            IndexKey k = new IndexKey();
            braf.seek(position);
            line = braf.readLine();

            pkList = new ArrayList<>();

            while (line != null) {

                if (line.equals("")) {
                    spec.setNumPeaks(pkList.size());
                    k.setPM(spec.getPCMass());
                    k.setScanNum(spec.getScanNumber());
                    spec.setIndex(k);
                    spec.setPeakList(pkList);
                    break;
                } else if (Character.isDigit(line.charAt(0))) {
                    String fline = line.replaceAll("\\s+", " ");
                    String[] p = fline.split(" ");
                    double pcm = Double.parseDouble(p[0]);
                    double pci = Double.parseDouble(p[1]);

                    String annotation = "";
                    if(p.length > 2){
                        annotation = p[2];
                    }
                    pk=new Peak(pcm, pci, annotation);  
                    pkList.add(pk);

                } else if (line.startsWith("Name")) {
                    k = new IndexKey();
                    k.setPos(braf.getFilePointer());
                    spec.setTitle(line.substring(line.indexOf(":") + 2));
                    spec.setSequence(line.substring(line.indexOf(":") + 2, line.indexOf("/")));
                    String ch = line.substring(line.indexOf('/') + 1);
                    spec.setCharge(ch);

                } else if (line.startsWith("MW")) {
                    spec.setMW(Double.parseDouble(line.substring(line.indexOf(":") + 2)));

                } else if (line.startsWith("Comment")) {
                    spec.setComment(line);
                    List<Modification> mods = getModifications(line, spec.getSequence());
                    spec.setModification(mods);    
                    String[] temp = line.split(" ");
                    int tempLen = temp.length;
                    for (int b = 0; b < tempLen; b++) {
                        if (temp[b].startsWith("Parent=")) {
                            double precmass = Double.parseDouble(temp[b].substring(temp[b].indexOf("=") + 1));
                            spec.setPCMass(precmass);
                            

                        }
                        else if (temp[b].startsWith("Protein=")) {
                            int indx=temp[b].indexOf("=") +1;                            
                            String prot = temp[b].substring(indx);
                            spec.setProtein(prot);
                        } else if (temp[b].startsWith("Scan=")) {
                            String scan = temp[b].substring(temp[b].indexOf("=") + 1);
                            spec.setScanNumber(scan);
                        }
                    }

                }
                line = braf.readLine();
            }

        } catch (IOException ex) {
            Logger.getLogger(MspReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return spec;
    }

}
