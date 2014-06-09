package com.dslplatform.compiler.client.params;

public enum LoggingLevel implements Param {

        INFO("INFO")
        , WARN("WARN")
        , ERROR("ERROR")
        , DEBUG("DEBUG")
        , TRACE("TRACE")
        , NONE("NONE");

    public final String level;

    private LoggingLevel(final String level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "Logging(" + level + ")";
    }

    public static boolean contains(final String string){
        for(final LoggingLevel loggingLevel : LoggingLevel.values()){
            if(loggingLevel.level.equals(string))
                return true;
        }
        return true;
    }

}
