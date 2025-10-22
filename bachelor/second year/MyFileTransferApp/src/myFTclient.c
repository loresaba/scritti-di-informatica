#include "myFTclient.h"

// Funzione principale
int main(int argc, char *argv[]) {
  int sock;
  struct sockaddr_in serv_addr;
  char buffer[BUFFER_SIZE];
  char *command = argv[1];
  char *server_address = NULL;
  int port = 0;
  char *local_path = NULL;
  char *remote_path = NULL;
  
  // Parsing degli argomenti della riga di comando
  for (int i = 2; i < argc; i += 2) {
    if (strcmp(argv[i], "-a") == 0) {
      server_address = argv[i + 1];
    } else if (strcmp(argv[i], "-p") == 0) {
      port = atoi(argv[i + 1]);
    } else if (strcmp(argv[i], "-f") == 0) {
      local_path = argv[i + 1];
    } else if (strcmp(argv[i], "-o") == 0) {
      remote_path = argv[i + 1];
    }
  }

  // Verifica del comando e dei parametri necessari
  if (strcmp(argv[1], "-w") != 0 && strcmp(argv[1], "-r") != 0 && strcmp(argv[1], "-l") != 0) {
    printf("Usage:\n"
             "%s -w -a server_address -p port -f local_path/filename_local -o remote_path/filename_remote\n"
             "%s -w -a server_address -p port -f local_path/filename_local\n"
             "%s -r -a server_address -p port -f remote_path/filename_remote -o local_path/filename_local\n"
             "%s -r -a server_address -p port -f remote_path/filename_remote\n"
             "%s -l -a server_address -p port -f remote_path/\n", argv[0], argv[0], argv[0], argv[0], argv[0]);
    exit(1);
  }
  
  // Verifica della sintassi dei comandi -w (write)
  if (strcmp(argv[1], "-w") == 0 && ((!server_address || port == 0 || !local_path) || 
                                     (server_address && port != 0 && local_path && !remote_path && argc!=8) ||
                                     (server_address && port != 0 && local_path && remote_path && argc!=10))) {
    printf("Usage:\n"
             "%s -w -a server_address -p port -f local_path/filename_local -o remote_path/filename_remote\n"
             "%s -w -a server_address -p port -f local_path/filename_local\n"
             , argv[0], argv[0]);
    exit(1);
  }
  
  // Verifica della sintassi dei comandi -r (read)
  if (strcmp(argv[1], "-r") == 0 && ((!server_address || port == 0 || !local_path) || 
                                     (server_address && port != 0 && local_path && !remote_path && argc!=8) ||
                                     (server_address && port != 0 && local_path && remote_path && argc!=10))) {
    printf("Usage:\n"
             "%s -r -a server_address -p port -f remote_path/filename_remote -o local_path/filename_local\n"
             "%s -r -a server_address -p port -f remote_path/filename_remote\n"
             , argv[0], argv[0]);
    exit(1);
  }
  
  // Verifica della sintassi dei comandi -l (list)
  if (strcmp(argv[1], "-l") == 0 && ((!server_address || port == 0 || !local_path || remote_path) || argc!=8)) {
    printf("Usage:\n"
             "%s -l -a server_address -p port -f remote_path/\n"
             , argv[0]);
    exit(1);
  }
  
  // Creazione del socket
  sock = socket(PF_INET, SOCK_STREAM, 0);
  if (sock == -1) error_handling("socket() error");
  
  // Impostazione dell'indirizzo del server
  memset(&serv_addr, 0, sizeof(serv_addr));
  serv_addr.sin_family = AF_INET;
  serv_addr.sin_addr.s_addr = inet_addr(server_address);
  serv_addr.sin_port = htons(port);
  
  // Connessione al server
  if (connect(sock, (struct sockaddr*)&serv_addr, sizeof(serv_addr)) == -1) {
    error_handling("connect() error");
  }
  
  // Esecuzione del comando
  if (strcmp(command, "-l") == 0) {
    list_files(sock, local_path);
  } else if (strcmp(command, "-w") == 0) {
    if (remote_path) {
      send_file(sock, local_path, remote_path);
    } else {
      send_file(sock, local_path, local_path);
    }
  } else if (strcmp(command, "-r") == 0) {
    if (remote_path) {
      receive_file(sock, local_path, remote_path);
    } else {
      receive_file(sock, local_path, local_path);
    }
  }
  
  // Chiusura del socket
  close(sock);
  return 0;
}

// Funzione per inviare un file al server
void send_file(int sock, const char *local_path, const char *remote_path) {
  char buffer[BUFFER_SIZE];
  
  // Ottieni la directory home dell'utente
  const char *home_dir = getenv("HOME");
  if (home_dir == NULL) {
    fprintf(stderr, "Failed to get the home directory.\n");
    exit(1);
  }
  
  // Concatenazione della directory home con il percorso locale
  char fullpath[BUFFER_SIZE];
  snprintf(fullpath, sizeof(fullpath), "%s/%s", home_dir, local_path);
  local_path = fullpath;
  
  // Verifica dell'esistenza del file locale
  struct stat path_stat;
  if (stat(local_path, &path_stat) == -1 || !S_ISREG(path_stat.st_mode)) {
    printf("error %s: No such file\n", local_path);
    return;
  }
  
  // Apertura del file locale
  FILE *file = fopen(local_path, "rb");
  if (file == NULL) {
    snprintf(buffer, sizeof(buffer), "fopen() error %s", local_path);
    perror(buffer);
    return;
  }
  
  // Invio del comando PUT e del percorso del file remoto
  snprintf(buffer, sizeof(buffer), "PUT %s", remote_path);
  write(sock, buffer, strlen(buffer));
  
  // Lettura dell'acknowledgment dal server
  int ack_len = read(sock, buffer, sizeof(buffer) - 1);
  if (ack_len <= 0) {
    perror("Server acknowledgment error");
    fclose(file);
    return;
  }
  buffer[ack_len] = '\0';
  if (strcmp(buffer, "ACK") != 0) {
    fprintf(stderr, "Expected ACK, got %s\n", buffer);
    fclose(file);
    return;
  }
  
  // Invio del contenuto del file
  printf("Start writing ... ");
  while (!feof(file)) {
    int bytes_read = fread(buffer, 1, sizeof(buffer), file);
    write(sock, buffer, bytes_read);
  }
  
  fclose(file);
  printf("Writing compleated\n");
}

// Funzione per ricevere un file dal server
void receive_file(int sock, const char *remote_path, const char *local_path) {
  FILE *file;
  char buffer[BUFFER_SIZE];
  
  // Invio del comando GET e del percorso del file remoto
  snprintf(buffer, sizeof(buffer), "GET %s", remote_path);
  write(sock, buffer, strlen(buffer));
  size_t bytes_received;
  
  // Lettura della risposta dal server
  int str_len = read(sock, buffer, sizeof(buffer) - 1);
  if (str_len < 0) {
    perror("read");
    return;
  }
  
  buffer[str_len] = '\0';
  if (strncmp(buffer, "error", 5) == 0) {
    printf("Server %s\n", buffer);
    return;
  }
  
  // Ottieni la directory home dell'utente
  const char *home_dir = getenv("HOME");
  if (home_dir == NULL) {
    fprintf(stderr, "Failed to get the home directory.\n");
    exit(1);
  }
  
  // Concatenazione della directory home con il percorso locale
  char fullpath[BUFFER_SIZE];
  snprintf(fullpath, sizeof(fullpath), "%s/%s", home_dir, local_path);
  local_path = fullpath;
  
  file = fopen(local_path, "wb");
  if (file == NULL) {
    if (create_full_path(local_path) == -1) {
      perror("create_full_path");
      return;
    }
  }
  
  // Apertura del file locale
  file = fopen(local_path, "wb");
  if (file == NULL) {
    perror("fopen() error");
    return;
  }
  
  printf("Start reading ... ");
  
  // Se riceviamo "EOF" come primo pacchetto, il file è vuoto
  if (strncmp(buffer, "EOF", 3) == 0) {
    fclose(file);
    printf("Reading compleated\n");
    return;
  }
  // Scrivi il contenuto del primo pacchetto nel file
  fwrite(buffer, 1, str_len, file);
  // Continua a ricevere e scrivere i dati fino a "EOF"
  while ((bytes_received = read(sock, buffer, sizeof(buffer))) > 0) {
    fwrite(buffer, 1, bytes_received, file);
  }
  
  fclose(file);
  printf("Reading compleated\n");
}

// Funzione per elencare i file presenti sul server
void list_files(int sock, const char *remote_path) {
  char buffer[BUFFER_SIZE];
  
  // Invia il comando LIST e il percorso del file remoto
  snprintf(buffer, sizeof(buffer), "LIST %s", remote_path);
  write(sock, buffer, strlen(buffer));
  
  int str_len = read(sock, buffer, sizeof(buffer) - 1);
  if (str_len <= 0) {
    perror("read");
    return;
  }
  
  buffer[str_len] = '\0';
  if (strncmp(buffer, "error", 5) == 0) {
    printf("Server %s\n", buffer);
    return;
  }
  
  // Se il server invia "END_OF_LIST", significa che la directory è vuota
  if (strcmp(buffer, END_OF_LIST) == 0) {
    printf("Empty directory\n");
    return;
  }
  
  // Stampa la lista dei file ricevuti dal server
  printf("List of files:\n");
  printf("%s", buffer);
  // Continua a leggere e stampare i dati fino a "END_OF_LIST"
  while ((str_len = read(sock, buffer, sizeof(buffer) - 1)) > 0) {
    buffer[str_len] = '\0';
    if (strcmp(buffer, END_OF_LIST) == 0) {
      break;
    }
    printf("%s", buffer);
  }
}

// Funzione per gestire gli errori, stampando un messaggio di errore
void error_handling(char *message) {
  perror(message);
  exit(1);
}

// Funzione per creare il percorso completo dei file, inclusa la creazione delle directory se necessario
int create_full_path(const char *path) {
  char *path_copy = strdup(path);
  if (path_copy == NULL) {
    perror("strdup() error");
    return -1;
  }
  
  char *dir_path = dirname(path_copy);
  char *dir_copy = strdup(dir_path);
  if (dir_copy == NULL) {
    perror("strdup() error");
    free(path_copy);
    return -1;
  }
  
  char *subdir;
  char *saveptr = NULL;
  char temp_path[BUFFER_SIZE] = "";
  // Gestisce il percorso assoluto controllando se inizia con '/'
  if (path[0] == '/') {
    strcpy(temp_path, "/");
  }
  // Itera attraverso le sotto-directory e crea le directory se non esistono
  for (subdir = strtok_r(dir_copy, "/", &saveptr); subdir != NULL; subdir = strtok_r(NULL, "/", &saveptr)) {
    strcat(temp_path, subdir);
    strcat(temp_path, "/");
    if (mkdir(temp_path, 0777) == -1 && errno != EEXIST) {
      perror("mkdir");
      free(path_copy);
      free(dir_copy);
      return -1;
    }
  }
  
  // Crea il file finale
  int fd = open(path, O_WRONLY | O_CREAT | O_TRUNC, 0666);
  if (fd == -1) {
    perror("open");
    free(path_copy);
    free(dir_copy);
    return -1;
  }
  close(fd);
  
  free(path_copy);
  free(dir_copy);
  return 0;
}