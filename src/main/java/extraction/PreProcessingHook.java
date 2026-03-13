package extraction;

public abstract class PreProcessingHook {

    
    /**
     * This method will be called before the mapping process begins.
    */
   public abstract void execute(ProcessingContext context);

   public abstract String getName();

    protected void log(String message) {
        System.out.println("[" + getName() + "] " + message);
    }
}
