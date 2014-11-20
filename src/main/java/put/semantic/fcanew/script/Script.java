/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.script;

import java.io.File;
import java.util.List;

/**
 *
 * @author smaug
 */
public interface Script {

    public List<File> getOntologies();

    public List<String> getAttributes();

    public boolean getGenerateNegation();

    public boolean getGenerateDomain();

    public boolean getGenerateRange();

    public int getClassifier();

    public boolean shouldRun();

    public boolean shouldLockLOD();

    public void submitLog(File logfile);

    public int[] getCalculators();
}
