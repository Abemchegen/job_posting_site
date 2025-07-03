package sample.project.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Experiance {
    private String experianceName;
    private String experianceDescription;
    private Integer experianceYear;
}
