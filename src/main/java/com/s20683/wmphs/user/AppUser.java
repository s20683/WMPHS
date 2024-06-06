package com.s20683.wmphs.user;

import com.s20683.wmphs.gui2wmphs.request.AppUserDTO;
import com.s20683.wmphs.order.CompletationOrder;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ResourceBundle;
import java.util.Set;

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

    @Transient
    private ResourceBundle bundle = ResourceBundle.getBundle("pickingMessages_pl");


    public AppUser(String name) {
        this.name = name;
    }

    public AppUserDTO toDTO(){
        return new AppUserDTO(id, name, processMessage);
    }

    @PostConstruct
    public void init(){
        processMessage = bundle.getString("processMessage.start");
    }

}