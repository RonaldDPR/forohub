CREATE TABLE topicos (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  titulo VARCHAR(120) NOT NULL,
  mensaje TEXT NOT NULL,
  fecha_creacion DATETIME NOT NULL,
  status VARCHAR(20) NOT NULL,
  id_autor BIGINT NOT NULL,
  activo TINYINT DEFAULT 1,

  CONSTRAINT fk_autor FOREIGN KEY (id_autor) REFERENCES usuarios(id)
);
