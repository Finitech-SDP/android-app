package uk.ac.ed.inf.finitech.structs;

import androidx.annotation.NonNull;

// See LIST-ROBOTS-RES
/*
{
  "TAG": "LIST-ROBOTS-RES":
  "DATA": {
    "robots": [
      {
        "id": "40a77f40-30cb-4a86-a80e-4da0289fc25a",
        "name": "Bender",
        "isControlled": false,
      }
    ]
  }
}
*/
public class Robot {
    public String id;
    public String name;
    public boolean isControlled;

    public Robot(String id, String name, boolean isControlled) {
        this.id = id;
        this.name = name;
        this.isControlled = isControlled;
    }

    @NonNull
    @Override
    public String toString() {
        return name + (isControlled ? " (controlled)" : "");
    }
}
