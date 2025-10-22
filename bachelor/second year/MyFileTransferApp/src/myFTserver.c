#include "myFTserver.h"

// Funzione principale
int main(int argc, char *argv[]) {
  // Verifica che il numero di argomenti sia corretto
  if (argc != 7) {
    printf("Usage: %s -a server_address -p server_port -d ft_root_directory\n", argv[0]);
    exit(1);
  }
  
  char *server_address = NULL;
  int server_port = 0;
  ft_root_directory = NULL;
  
  // Parsing degli argomenti della riga di comando
  for (int i = 1; i < argc; i += 2) {
    if (strcmp(argv[i], "-a") == 0) {
      server_address = argv[i + 1];
    } else if (strcmp(argv[i], "-p") == 0) {
      server_port = atoi(argv[i + 1]);
    } else if (strcmp(argv[i], "-d") == 0) {
      ft_root_directory = argv[i + 1];
    }
  }
  
  // Verifica che tutti i parametri necessari siano stati forniti
  if (!server_address || server_port == 0 || !ft_root_directory) {
    printf("Usage: %s -a server_address -p server_port -d ft_root_directory\n", argv[0]);
    exit(1);
  }
  
  // Ottieni la directory home dell'utente
  const char *home_dir = getenv("HOME");
  if (home_dir == NULL) {
    fprintf(stderr, "Failed to get the home directory.\n");
    exit(1);
  }
  
  // Concatena la directory home con ft_root_directory
  char fullpath[BUFFER_SIZE];
  snprintf(fullpath, sizeof(fullpath), "%s/%s", home_dir, ft_root_directory);
  ft_root_directory = fullpath;
  
  // Verifica se ft_root_directory esiste o la crea se non esiste
  struct stat st;
  if (stat(ft_root_directory, &st) == -1) {
    if (errno == ENOENT) {
      // La directory non esiste, quindi la crea
      if (mkdir(ft_root_directory, 0777) == -1) {
        perror("mkdir");
        exit(1);
      }
    } else {
      perror("stat");
      exit(1);
    }
  } else if (!S_ISDIR(st.st_mode)) {
    // Se il percorso esiste ma non è una directory
    fprintf(stderr, "error %s: exists and is not a directory\n", ft_root_directory);
    exit(1);
  }
  
  // Creazione del socket del server
  int serv_sock;
  struct sockaddr_in serv_addr;
  
  serv_sock = socket(PF_INET, SOCK_STREAM, 0);
  if (serv_sock == -1) error_handling("socket() error");
  
  memset(&serv_addr, 0, sizeof(serv_addr));
  serv_addr.sin_family = AF_INET;
  serv_addr.sin_addr.s_addr = inet_addr(server_address);
  serv_addr.sin_port = htons(server_port);
  
  // Binding del socket
  if (bind(serv_sock, (struct sockaddr*)&serv_addr, sizeof(serv_addr)) == -1) {
    error_handling("bind() error");
  }
  
  // Mette il socket in ascolto
  if (listen(serv_sock, 5) == -1) {
    error_handling("listen() error");
  }
  
  // Messaggio di avvio del server
  printf("Server started on %s:%d in %s\n", server_address, server_port, ft_root_directory);
  
  // Accetta e gestisce le connessioni in entrata
  while (1) {
    struct sockaddr_in client_addr;
    socklen_t client_addr_size = sizeof(client_addr);
    int client_sock = accept(serv_sock, (struct sockaddr*)&client_addr, &client_addr_size);
    
    if (client_sock == -1) {
      perror("accept() error");
      continue;
    }
    
    // Crea un thread per gestire il client
    pthread_t t_id;
    int *client_sock_ptr = malloc(sizeof(int));
    *client_sock_ptr = client_sock;
    pthread_create(&t_id, NULL, handle_client, client_sock_ptr);
    pthread_detach(t_id);
  }
  
  close(serv_sock); // Chiude il socket del server
  return 0;
}

// Funzione per gestire un client in un thread separato
void *handle_client(void *arg) {
  int client_sock = *((int*)arg);
  free(arg);
  
  char buffer[BUFFER_SIZE];
  int str_len;
  
  // Ciclo per gestire i comandi inviati dal client
  //while ((str_len = read(client_sock, buffer, sizeof(buffer) - 1)) != 0) {
  str_len = read(client_sock, buffer, sizeof(buffer) - 1);
  if (str_len == -1) {
    perror("read() error");
    close(client_sock);
    return NULL;
  }
    
  buffer[str_len] = '\0';
  char command[5], filepath[BUFFER_SIZE];
  char fullpath[BUFFER_SIZE];
  
  // Parsing del comando e del percorso del file dal buffer ricevuto
  sscanf(buffer, "%4s %s", command, filepath);
  snprintf(fullpath, sizeof(fullpath), "%s/%s", ft_root_directory, filepath);
  printf("%s %s\n", command, fullpath);
  
  // Gestione dei diversi comandi inviati dal client
  if (strcmp(command, "LIST") == 0) {
    send_file_list(client_sock, fullpath);
  } else if (strcmp(command, "GET") == 0) {
    send_file(client_sock, fullpath);
  } else if (strcmp(command, "PUT") == 0) {
    write(client_sock, "ACK", 3);
    receive_file(client_sock, fullpath);
  } else {
    printf("Unknown command: %s\n", command);
  }
  
  close(client_sock); // Chiude il socket del client
  return NULL;
}

// Funzione per inviare la lista dei file in una directory al client
void send_file_list(int client_sock, const char *path) {
  DIR *dir;
  char fullpath[BUFFER_SIZE];
  char buffer[BUFFER_SIZE];
  char command[BUFFER_SIZE];
  FILE *fp;
  
  // Apre la directory specificata
  snprintf(fullpath, sizeof(fullpath), "%s", path);
  dir = opendir(fullpath);
  
  // Gestisce il caso in cui la directory non esista
  if (dir == NULL) {
    snprintf(buffer, sizeof(buffer), "error %s: No such directory", fullpath);
    write(client_sock, buffer, strlen(buffer));
    return;
  }
  
  // Costruisce il comando per ottenere la lista dei file nella directory
  snprintf(command, sizeof(command), "find %s -maxdepth 1 -type f -exec ls -la {} +", path);
  
  // Apre il comando per la lettura
  fp = popen(command, "r");
  if (fp == NULL) {
    snprintf(buffer, sizeof(buffer), "error %s: Failed to execute command", command);
    write(client_sock, buffer, strlen(buffer));
    return;
  }
  
  // Legge l'output riga per riga e lo invia al client
  while (fgets(buffer, sizeof(buffer), fp) != NULL) {
    write(client_sock, buffer, strlen(buffer));
  }
  
  pclose(fp); // Chiude il puntatore al file
  closedir(dir); // Chiude la directory aperta
  write(client_sock, END_OF_LIST, strlen(END_OF_LIST)); // Invia segnale di fine lista
}

// Funzione per inviare un file al client
void send_file(int client_sock, const char *path) {
  char buffer[BUFFER_SIZE];
  size_t bytes_read;
  
  struct stat path_stat;
  // Verifica che il percorso sia un file regolare e che esista
  if (stat(path, &path_stat) == -1 || !S_ISREG(path_stat.st_mode)) {
    snprintf(buffer, sizeof(buffer), "error %s: No such file", path);
    write(client_sock, buffer, strlen(buffer));
    return;
  }
  
  pthread_mutex_lock(&file_mutex); // Blocca il mutex per l'accesso al file
  
  FILE *file = fopen(path, "rb");
  if (file == NULL) {
    snprintf(buffer, sizeof(buffer), "error %s: %s", path, strerror(errno));
    write(client_sock, buffer, strlen(buffer));
    pthread_mutex_unlock(&file_mutex);
    return;
  }
  
  // Legge il file e lo invia al client per blocchi di dimensione BUFFER_SIZE
  while ((bytes_read = fread(buffer, 1, sizeof(buffer), file)) > 0) {
    write(client_sock, buffer, bytes_read);
  }
  
  fclose(file); // Chiude il file
  pthread_mutex_unlock(&file_mutex); // Sblocca il mutex
}

// Funzione per ricevere un file dal client
void receive_file(int client_sock, const char *path) {
  FILE *file;
  char buffer[BUFFER_SIZE];
  size_t bytes_received;
  
  pthread_mutex_lock(&file_mutex); // Blocca il mutex per l'accesso al file
  
  // Apre il file in modalità scrittura binaria
  file = fopen(path, "wb");
  if (file == NULL) {
    // Se fallisce, prova a creare le directory necessarie
    if (create_full_path(path) == -1) {
      perror("create_full_path");
      write(client_sock, "ERROR: Cannot create path", 25);
      pthread_mutex_unlock(&file_mutex);
      return;
    }
  }
  
  // Riapre il file in modalità scrittura binaria
  file = fopen(path, "wb");
  if (file == NULL) {
    perror("fopen() error");
    write(client_sock, "ERROR: Cannot open file", 24);
    pthread_mutex_unlock(&file_mutex);
    return;
  }
  
  // Legge i dati dal socket e li scrive nel file
  while ((bytes_received = read(client_sock, buffer, sizeof(buffer))) > 0) {
    fwrite(buffer, 1, bytes_received, file);
  }
  
  fclose(file); // Chiude il file
  pthread_mutex_unlock(&file_mutex); // Sblocca il mutex
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