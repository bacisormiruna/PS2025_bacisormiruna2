package com.example.demo.dto.frienddto;

import lombok.Data;

@Data
public class FriendDTO {
    private Long id;
    private String name;
    private String status;

    public FriendDTO(Long id, String name, String status) {
        this.id = id;
        this.name = name;
        this.status=status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

