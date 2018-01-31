package com.compomics.ms2io;

/**
 *
 * @author Genet
 */
public class Peak  {
    
    
    /**
     * m/z value of the peak
     */
    public double mz;
   
    /**
     * intensity of the peak.
     */
    public double intensity;

    /**
     * class constructor
     *
     * @param mz the m/z value of the peak
     * @param intensity the intensity of the peak
     */
    public Peak(double mz, double intensity) {
        this.mz = mz;
        this.intensity = intensity;
    }

    /**
     * Returns true if the peak has the same m/z and intensity.
     *
     * @param p the peal to compare this peak to
     * @return true if the peak has the same m/z and intensity
     */
    public boolean isEqual(Peak p) {
      
        return mz == p.mz && intensity == p.intensity;
    }



    /**
     * Returns the mz.
     *
     * @return the mz
     */
    public double getMz() {
        return mz;
    }

    /**
     * Set the mz.
     *
     * @param mz the value to set
     */
    public void setMz(double mz) {
        this.mz = mz;
    }

    /**
     * Returns the intensity.
     *
     * @return the intensity
     */
    public double getIntensity() {
        return intensity;
    }

    /**
     * Set the intensity.
     *
     * @param intensity the intensity to set
     */
    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

 

}