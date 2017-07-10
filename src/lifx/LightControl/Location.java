package lifx.LightControl;

import lifx.LightControl.Group;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Location {
	//Location class will house Group class and allow controls to entire location
	private String id;
	private String name;
	public Group[] Group;
	public int numLights;
	public int numGroups;
	
	private String selector;
	/*Following variables are true only if all lights in group are the same value for these properties
	 * -1 if they are not the same, otherwise the value is correct for all lights.
	 */
	private int power; //-1 for not the same, 0 for off, 1 for on
	private float hue;
	private float saturation;
	private float kelvin;
	private float brightness;
	
	public Location(String idIn, String nameIn){
		//Remove quotation marks from strings
		if(idIn.startsWith("\"") && idIn.endsWith("\"")) idIn = idIn.substring(1, idIn.length()-1);
		if(nameIn.startsWith("\"") && nameIn.endsWith("\"")) nameIn = nameIn.substring(1, nameIn.length()-1);
		id = idIn;
		name = nameIn;
		selector = "location_id:" + id;
		
		String lifxLocationString = LightFunctions.listLights("location_id:" + id);
		//System.out.println(lifxLocationString);
		
		JsonElement lifxJsonElement = new JsonParser().parse(lifxLocationString); //Holds everything
		JsonArray lightArray = lifxJsonElement.getAsJsonArray(); //Also everything
		//Lifx Json returns an array with each individual light in it
		
		//Put group objects in an ArrayList for first time (because dynamic)
		ArrayList<Group> groupList = new ArrayList<Group>(); //Contains type lifx.LightControl.group for each location
		JsonObject lightObject;
		JsonObject lightLocation;
		String currentId;
		
		int cont = 0;
		
		numLights = lightArray.size(); //Number of lights in location
		//Go through each light, find its location, and create that location as an object
		for (int i = 0; i < numLights; i++){
			
			lightObject = lightArray.get(i).getAsJsonObject();
			lightLocation = lightObject.get("group").getAsJsonObject();
			currentId = lightLocation.get("id").toString();
			
			int locationListSize = groupList.size();
			for (int j = 0; j < locationListSize; j++)
				//If location is already accounted for then 
				if (currentId.equals(((Group) groupList.get(j)).getId())) { 
					cont = 1;
					break;
				}
			if (cont == 1) continue;
			
			//Creates a new Group class for the current group
			Group currentGroup = new Group(currentId,lightLocation.get("name").toString());
			if (currentGroup != null) System.out.println("	Group object \"" + currentGroup.getName() + "\" created.");
			groupList.add(currentGroup);
		}
		numGroups = groupList.size();
		Group = new Group[groupList.size()];
		//Put ArrayList into array. Efficiency?
		for (int i = 0; i < groupList.size(); i++) Group[i] = groupList.get(i);
		
		groupList = null;
		lifxJsonElement = null;
	}

	/** Requests new information from LIFX server for the group
	 * and updates that information in the class. This is to be done before each
	  time a public or private "get" or "is" method is used.
	 */
	private void updateInfo(){
		if (numLights <= 0) return;
		String lifxGroupString = LightFunctions.listLights(selector);
		JsonElement lifxJsonElement = new JsonParser().parse(lifxGroupString);
		JsonArray lightJsonArray = lifxJsonElement.getAsJsonArray();
		JsonObject lightJsonObject = lightJsonArray.get(0).getAsJsonObject();
		
		String powerString = LightFunctions.removeQuotes(((String) lightJsonObject.get("power").getAsString()));
		if (powerString.equals("on")) power = 1;
		else power = 0;
		hue = lightJsonObject.get("hue").getAsFloat();
		saturation = lightJsonObject.get("saturation").getAsFloat();
		kelvin = lightJsonObject.get("kelvin").getAsFloat();
		brightness = lightJsonObject.get("brightness").getAsFloat();
		
		if (numLights == 1) { lifxJsonElement = null; return; }
		//Compare each current property value to the property of the next light.
		for (int i = 1; i < numLights; i++){
			if (power != -1){ //Damn strings make this more difficult than it needs to be
				powerString = LightFunctions.removeQuotes(((String) lightJsonObject.get("power").getAsString()));
				float powerCheck = power;
				if (powerString.equals("on")) power = 1;
				else power = 0;
				if (power != powerCheck) power = -1;
			}
			if ((hue != -1) && (hue != lightJsonObject.get("hue").getAsFloat())) hue = -1;
			if ((saturation != -1) && (saturation != lightJsonObject.get("saturation").getAsFloat())) saturation = -1;
			if ((kelvin != -1) && (kelvin != lightJsonObject.get("kelvin").getAsFloat())) kelvin = -1;
			if ((brightness != -1) && (brightness != lightJsonObject.get("brightness").getAsFloat())) brightness = -1;
		}
		lifxJsonElement = null;
	}
	
	/**Turns on power for this location.*/
	public void turnOn(){
		LightFunctions.turnOn(selector);
	}
	
	/**Turns off power for this location.*/
	public void turnOff(){
		LightFunctions.turnOff(selector);
	}
	
	/**Sets the color of all lights in this location. 
	 * Input is a hex colour without # before it */
	public void setColor(String hexColor){
		LightFunctions.setColour(selector, hexColor);
	}
	
	/**Brightness level between 0.0 and 1.0*/
	public void setBrightness(float brightnessLevel){
		LightFunctions.setBrightness(selector, brightnessLevel);
	}
	
	public String getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public int getPower(){
		updateInfo();
		return power;
	}
	
	public float getHue(){
		updateInfo();
		return hue;
	}
	
	public float getSaturation(){
		updateInfo();
		return saturation;
	}
	
	public float getKelvin(){
		updateInfo();
		return kelvin;
	}
	
	public float getBrightness(){
		updateInfo();
		return brightness;
	}

	public int size(){
		return numLights;
	}
}
