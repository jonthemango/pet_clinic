package org.springframework.samples.petclinic.toggles;

import net.minidev.json.JSONObject;

public class Toggle{
    public String name;
    public Boolean value;
    public Toggle(String name, Boolean value){
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        JSONObject obj = new JSONObject();
        obj.put("name", this.name);
        obj.put("value", this.value);
        return obj.toString();
    }
}
