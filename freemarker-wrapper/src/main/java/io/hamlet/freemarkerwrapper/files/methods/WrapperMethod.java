package io.hamlet.freemarkerwrapper.files.methods;

import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import io.hamlet.freemarkerwrapper.NumberOfArgumentsException;
import io.hamlet.freemarkerwrapper.WrapperCloneNotSupportedException;
import io.hamlet.freemarkerwrapper.WrapperIOException;
import io.hamlet.freemarkerwrapper.files.meta.Meta;
import io.hamlet.freemarkerwrapper.files.processors.Processor;

import java.io.IOException;
import java.util.List;

public abstract class WrapperMethod implements TemplateMethodModelEx {

    protected Meta meta;
    protected TemplateHashModelEx options;
    protected Processor processor;

    protected String methodName;
    protected int numberOfArguments;
    protected int minNumberOfArguments;
    protected int maxNumberOfArguments;

    public WrapperMethod(int numberOfArguments, String methodName) {
        this.numberOfArguments = numberOfArguments;
        this.methodName = methodName;
    }

    public WrapperMethod(String methodName, int minNumberOfArguments, int maxNumberOfArguments) {
        this.methodName = methodName;
        this.minNumberOfArguments = minNumberOfArguments;
        this.maxNumberOfArguments = maxNumberOfArguments;
    }

    public abstract TemplateModel process() throws TemplateModelException, IOException, CloneNotSupportedException;

    protected void verifyArguments(List args) throws NumberOfArgumentsException {
        if(minNumberOfArguments!= 0 || maxNumberOfArguments!=0 ){
            if( args.size() < minNumberOfArguments || args.size() > maxNumberOfArguments){
                throw new NumberOfArgumentsException(minNumberOfArguments, maxNumberOfArguments, args.size(), methodName);
            }
        }
        else if (args.size() != numberOfArguments) {
            throw new NumberOfArgumentsException(numberOfArguments, args.size(), methodName);
        }
    }

    public TemplateModel exec(List args) throws TemplateModelException {
        verifyArguments(args);
        init();
        parseArguments(args);
        TemplateModel templateModel;
        try {
            templateModel = process();
        } catch (IOException e){
            throw new WrapperIOException(e);
        } catch (CloneNotSupportedException e) {
            throw new WrapperCloneNotSupportedException(e);
        }
        return templateModel;
    }

    protected abstract void init();
    protected abstract void parseArguments(List args) throws TemplateModelException;
}
