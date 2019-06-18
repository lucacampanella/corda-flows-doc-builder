package com.github.lucacampanella.callgraphflows.graphics.svg.preferences;

public class DefaultPreferences implements PreferencesInterface {

    private static final double BRIGHTER_FACTOR = 1.2;

    // static variable singleInstance of type Singleton
    private static DefaultPreferences singleInstance = null;

    // private constructor restricted to this class itself
    private DefaultPreferences()
    {
    }

    // static method to create instance of Singleton class
    public static DefaultPreferences getInstance()
    {
        if (singleInstance == null)
            singleInstance = new DefaultPreferences();

        return singleInstance;
    }

    @Override
    public double getBrighterFactor() {
        return BRIGHTER_FACTOR;
    }
}
