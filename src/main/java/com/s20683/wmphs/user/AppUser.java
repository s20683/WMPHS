package com.s20683.wmphs.user;

import com.s20683.wmphs.gui2wmphs.request.AppUserDTO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "AppUser")
@NoArgsConstructor
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 50, nullable = false)
    private String name;

    @Transient
    private String processMessage = "";


    public AppUser(String name) {
        this.name = name;
    }

    public AppUserDTO toDTO(){
        return new AppUserDTO(id, name, processMessage);
    }

}