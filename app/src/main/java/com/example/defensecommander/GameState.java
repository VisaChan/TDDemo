package com.example.defensecommander;

public class GameState {
    private static boolean Freeze = false;

    public static void changeFreeze(){
        if (Freeze == true){
            Freeze = false;
        }
        else{
            Freeze = true;
        }
    }

    public static boolean checkFreeze(){
        return Freeze;
    }
}
