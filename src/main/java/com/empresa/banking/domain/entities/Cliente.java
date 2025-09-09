package com.empresa.banking.domain.entities;

import com.empresa.banking.domain.entities.Enums.TipoIdentificacion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Objects;

public class Cliente {

    private final Long id;
    private final TipoIdentificacion tipoIdentificacion;
    private final String numeroIdentificacion;
    private final String nombres;
    private final String apellido;
    private final String correoElectronico;
    private final LocalDate fechaNacimiento;
    private final LocalDateTime fechaCreacion;
    private final LocalDateTime fechaModificacion;

    // Constructor principal
    public Cliente(Long id, TipoIdentificacion tipoIdentificacion, String numeroIdentificacion,
                   String nombres, String apellido, String correoElectronico,
                   LocalDate fechaNacimiento, LocalDateTime fechaCreacion,
                   LocalDateTime fechaModificacion) {
        this.id = id;
        this.tipoIdentificacion = Objects.requireNonNull(tipoIdentificacion, "El tipo de identificación es obligatorio");
        this.numeroIdentificacion = validarNumeroIdentificacion(numeroIdentificacion);
        this.nombres = validarNombres(nombres);
        this.apellido = validarApellido(apellido);
        this.correoElectronico = validarCorreo(correoElectronico);
        this.fechaNacimiento = validarFechaNacimiento(fechaNacimiento);
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : LocalDateTime.now();
        this.fechaModificacion = fechaModificacion;
    }

    // Constructor para creación (sin ID)
    public static Cliente crear(TipoIdentificacion tipoIdentificacion, String numeroIdentificacion,
                                String nombres, String apellido, String correoElectronico,
                                LocalDate fechaNacimiento) {
        return new Cliente(null, tipoIdentificacion, numeroIdentificacion, nombres,
                apellido, correoElectronico, fechaNacimiento,
                LocalDateTime.now(), null);
    }

    // Constructor para actualización
    public Cliente actualizar(String nombres, String apellido, String correoElectronico) {
        return new Cliente(this.id, this.tipoIdentificacion, this.numeroIdentificacion,
                nombres != null ? nombres : this.nombres,
                apellido != null ? apellido : this.apellido,
                correoElectronico != null ? correoElectronico : this.correoElectronico,
                this.fechaNacimiento, this.fechaCreacion, LocalDateTime.now());
    }

    // Validaciones de negocio
    private String validarNumeroIdentificacion(String numeroIdentificacion) {
        if (numeroIdentificacion == null || numeroIdentificacion.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de identificación es obligatorio");
        }
        return numeroIdentificacion.trim();
    }

    private String validarNombres(String nombres) {
        if (nombres == null || nombres.trim().length() < 2) {
            throw new IllegalArgumentException("Los nombres deben tener al menos 2 caracteres");
        }
        return nombres.trim();
    }

    private String validarApellido(String apellido) {
        if (apellido == null || apellido.trim().length() < 2) {
            throw new IllegalArgumentException("El apellido debe tener al menos 2 caracteres");
        }
        return apellido.trim();
    }

    private String validarCorreo(String correoElectronico) {
        if (correoElectronico == null || !correoElectronico.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("El formato del correo electrónico no es válido");
        }
        return correoElectronico.toLowerCase().trim();
    }

    private LocalDate validarFechaNacimiento(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento debe estar en el pasado");
        }

        int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
        if (edad < 18) {
            throw new IllegalArgumentException("El cliente debe ser mayor de edad");
        }

        return fechaNacimiento;
    }

    // Métodos de negocio
    public boolean esMayorDeEdad() {
        return Period.between(fechaNacimiento, LocalDate.now()).getYears() >= 18;
    }

    public int getEdad() {
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    // Getters
    public Long getId() { return id; }
    public TipoIdentificacion getTipoIdentificacion() { return tipoIdentificacion; }
    public String getNumeroIdentificacion() { return numeroIdentificacion; }
    public String getNombres() { return nombres; }
    public String getApellido() { return apellido; }
    public String getCorreoElectronico() { return correoElectronico; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(numeroIdentificacion, cliente.numeroIdentificacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroIdentificacion);
    }
}
