package com.github.lucacampanella.callgraphflows.graphics.preferences;

public class DefaultPreferences implements PreferencesInterface {

    private static double BRIGHTER_FACTOR = 1.2;

//    public static final Color LESS_IMPORTANT_TEXT_COLOR = Color.GRAY;

    // static variable single_instance of type Singleton
    private static DefaultPreferences single_instance = null;

    // private constructor restricted to this class itself
    private DefaultPreferences()
    {
    }

    // static method to create instance of Singleton class
    public static DefaultPreferences getInstance()
    {
        if (single_instance == null)
            single_instance = new DefaultPreferences();

        return single_instance;
    }

    @Override
    public double getBrighterFactor() {
        return BRIGHTER_FACTOR;
    }

//    @Override
//    public Color getLessImportantTextColor() {
//        return LESS_IMPORTANT_TEXT_COLOR;
//    }
}
