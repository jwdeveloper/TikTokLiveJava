package io.github.jwdeveloper.tiktok.data.models.chest;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Chest {

    /**
     * Total diamonds inside the chest
     */
    int totalDiamonds;

    /**
     * Total users participated in chest
     */
    int totalUsers;



}
