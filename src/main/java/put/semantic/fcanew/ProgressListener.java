package put.semantic.fcanew;

/**
 *
 * @author smaug
 */
public interface ProgressListener {

    public void reset(int max);

    public void update(int status);
}
