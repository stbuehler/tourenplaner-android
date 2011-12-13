package de.uni.stuttgart.informatik.ToureNPlaner.Data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

public class AlgorithmInfo implements Serializable {
    private String version;
    private String name;
    private String urlsuffix;
    private ArrayList<Constraint> point_constraints ;
    private int minPoints;
    private boolean sourceIsTarget;
    private boolean isHidden;
    @Override
    public String toString() {
        return name;
    }

    public boolean sourceIsTarget() {
        return sourceIsTarget;
    }

    public String getUrlsuffix() {
        return urlsuffix;
    }
    
    public ArrayList<Constraint> getPointConstraints(){
		return point_constraints;
    }
    
    public boolean getIsHidden(){
    	return isHidden;
    }
    
  public static AlgorithmInfo parse(JSONObject object) throws JSONException {
        AlgorithmInfo info = new AlgorithmInfo();
        info.version = object.getString("version");
        info.name = object.getString("name");
        info.urlsuffix = object.getString("urlsuffix");
        info.minPoints = object.getJSONObject("constraints").getInt("minPoints");
        info.sourceIsTarget = object.getJSONObject("constraints").getBoolean("sourceIsTarget");
        info.isHidden = object.getBoolean("hidden");
        
        if (object.isNull("pointconstraints")) {
              info.point_constraints = new ArrayList<Constraint>();
            } else {
        	JSONArray array = object.getJSONArray("pointconstraints");
            info.point_constraints = new ArrayList<Constraint>(array.length());
            for(int i=0;i<array.length();i++) {
               info.point_constraints.add(Constraint.parse(array.getJSONObject(i)));
            }
        }
        return info;
    }
}
