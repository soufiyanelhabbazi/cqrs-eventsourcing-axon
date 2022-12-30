package org.enset.comptecqrses.commonapi.dtos;


import lombok.Data;

@Data
public class DebitAccountRequestDTO {
    private String accountId;
    private double amount;
    private String currency;
}
