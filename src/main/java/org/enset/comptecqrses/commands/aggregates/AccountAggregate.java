package org.enset.comptecqrses.commands.aggregates;


import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.enset.comptecqrses.commonapi.commands.CreateAccountCommand;
import org.enset.comptecqrses.commonapi.commands.CreditAccountCommand;
import org.enset.comptecqrses.commonapi.commands.DebitAccountCommand;
import org.enset.comptecqrses.commonapi.enums.AccountStatus;
import org.enset.comptecqrses.commonapi.events.AccountActivatedEvent;
import org.enset.comptecqrses.commonapi.events.AccountCreatedEvent;
import org.enset.comptecqrses.commonapi.events.AccountCreditedEvent;
import org.enset.comptecqrses.commonapi.events.AccountDebitedEvent;
import org.enset.comptecqrses.commonapi.exceptions.AmountNegativeException;
import org.enset.comptecqrses.commonapi.exceptions.BalanceNotSufficientException;

@Aggregate
public class AccountAggregate {
    @AggregateIdentifier
    private String accountId;
    private double balance;
    private String currency;
    private AccountStatus accountStatus;


    public AccountAggregate() {
        //Required by axon
    }
    @CommandHandler
    public AccountAggregate(CreateAccountCommand createAccountCommand) {
        if(createAccountCommand.getInitialBalance()<0) throw new RuntimeException("Impossible...");
        AggregateLifecycle.apply(new AccountCreatedEvent(
                createAccountCommand.getId(),
                createAccountCommand.getInitialBalance(),
                createAccountCommand.getCurrency(),
                AccountStatus.CREATED
        ));
    }
    @EventSourcingHandler
    public void on(AccountCreatedEvent event){
        this.accountId= event.getId();
        this.balance= event.getInitialBalance();
        this.currency=event.getCurrency();
        this.accountStatus=AccountStatus.CREATED;
        AggregateLifecycle.apply(new AccountActivatedEvent(
                event.getId(),
                AccountStatus.ACTIVATED
        ));
    }
    @EventSourcingHandler
    public void on(AccountActivatedEvent event){

        this.accountStatus=event.getStatus();
    }

    @CommandHandler
    public void handle(CreditAccountCommand command){
        if(command.getAmount()<0) throw new AmountNegativeException("Amount should not be negative");
        AggregateLifecycle.apply(new AccountCreditedEvent(
                command.getId(),
                command.getAmount(),
                command.getCurrency()
        ));
    }

    @EventSourcingHandler
    public void on(AccountCreditedEvent event){
        this.balance+=event.getAmount();
    }

    @CommandHandler
    public void handle(DebitAccountCommand command){
        if(command.getAmount()<0) throw new AmountNegativeException("Amount should not be negative");
        if(this.balance<command.getAmount()) throw new BalanceNotSufficientException("Balance not sufficient exception");
        AggregateLifecycle.apply(new AccountDebitedEvent(
                command.getId(),
                command.getAmount(),
                command.getCurrency()
        ));
    }

    @EventSourcingHandler
    public void on(AccountDebitedEvent event){
        this.balance-=event.getAmount();
    }

}
