package io.hamlet.freemarkerwrapper.files.processors.validate;

import net.jimblackler.jsonschemafriend.SchemaException;
import io.hamlet.freemarkerwrapper.files.meta.Meta;
import io.hamlet.freemarkerwrapper.files.meta.validate.JsonMeta;
import io.hamlet.freemarkerwrapper.files.processors.Processor;
import net.jimblackler.jsonschemafriend.Schema;
import net.jimblackler.jsonschemafriend.SchemaStore;
import net.jimblackler.jsonschemafriend.Validator;

import java.util.HashMap;
import java.util.Map;

public class ValidateProcessor extends Processor {

    public Map toMethod(Meta meta) {
        JsonMeta jsonMeta = (JsonMeta) meta;
        String document = jsonMeta.getDocument();
        String schemaString = jsonMeta.getSchema();
        Map result = new HashMap();
        result.put("Status", 0);

        try {
            SchemaStore schemaStore = new SchemaStore();
            Schema schema = schemaStore.loadSchemaJson(schemaString);
            Validator validator = new Validator();
            validator.validateJson(schema, document);
        }catch (SchemaException e) {
            result.put("Status", -1);
            result.put("Logs", e.getStackTrace());
        }
        return result;
    }
}
