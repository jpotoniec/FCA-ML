package put.semantic.fcanew;

/**
 *
 * @author smaug
 */
public interface Expert {

    public enum Decision {

        SKIP, ACCEPT, REJECT
    };

    public Decision verify(Implication impl);

    //it's expert's responsibility to update context accordingly
//    public POD getCounterexample();
}
