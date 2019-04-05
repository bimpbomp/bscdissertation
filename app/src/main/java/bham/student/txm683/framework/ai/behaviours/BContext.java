package bham.student.txm683.framework.ai.behaviours;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class BContext {

    private Map<BKeyType, Object> compulsoryFields;

    private Map<String, Object> variables;

    public BContext(){
        this.compulsoryFields = new HashMap<>();
        this.variables = new HashMap<>();
    }

    public void addVariable(String key, Object value){
        variables.put(key, value);
    }

    public boolean containsVariables(String... keys){
        if (keys == null || keys.length == 0)
            return false;

        for (String key : keys){
            if (!variables.containsKey(key))
                return false;
        }
        return true;
    }

    public void removeVariables(String... keys){

        if (keys == null || keys.length == 0)
            return;

        for (String key : keys){
            variables.remove(key);
        }
    }

    public Object getVariable(String key){
        return variables.getOrDefault(key, null);
    }

    public Object variableOrDefault(String key, Object defaultValue){
        return variables.getOrDefault(key, defaultValue);
    }

    public boolean containsCompulsory(BKeyType... keys){
        if (keys == null || keys.length == 0)
            return false;

        for (BKeyType key : keys) {
            //if the key isn't in the context, return false
            if (!compulsoryFields.containsKey(key))
                return false;


            //check through all classes and interfaces in the inheritance chain of this item,
            //checking for if one of the classes is of the type stated in the Key's BKeyType enum
            Object item = compulsoryFields.get(key);

            if (item == null)
                continue;

            Class aClass = item.getClass();

            StringBuilder stringBuilder = new StringBuilder();
            while (aClass != null && !aClass.equals(Object.class)){

                stringBuilder.append(" class: ");

                stringBuilder.append(aClass.getSimpleName());
                stringBuilder.append(", ");
                if (aClass.equals(key.getType()))
                    return true;

                //check interfaces of the class
                stringBuilder.append("...");

                stringBuilder.append(" Interfaces: ");

                for (Class implem : aClass.getInterfaces()){
                    stringBuilder.append(implem.getSimpleName());
                    stringBuilder.append(", ");
                    if (implem.equals(key.getType()))
                        return true;
                }
                stringBuilder.append("EOI");

                aClass = aClass.getSuperclass();
            }
            Log.d("TESTING", stringBuilder.toString());
        }
        return false;
    }

    public void addCompulsory(BKeyType key, Object value){
        this.compulsoryFields.put(key, value);
    }

    public void removeCompulsory(BKeyType key){
        this.compulsoryFields.remove(key);
    }

    public Object getCompulsory(BKeyType key){
        return compulsoryFields.getOrDefault(key, null);
    }
}
