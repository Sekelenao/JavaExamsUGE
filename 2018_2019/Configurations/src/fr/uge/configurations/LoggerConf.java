package fr.uge.configurations;

import java.util.Objects;
import java.util.Optional;

public class LoggerConf {

    private String name;

    private LogLevel level;

    public Optional<String> name(){
        return Optional.ofNullable(name);
    }

    public LoggerConf name(String name){
        this.name = Objects.requireNonNull(name);
        return this;
    }

    public Optional<LogLevel> level(){
        return Optional.ofNullable(level);
    }

    public LoggerConf level(LogLevel level){
        this.level = Objects.requireNonNull(level);
        return this;
    }

}
