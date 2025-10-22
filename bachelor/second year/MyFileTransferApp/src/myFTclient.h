#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <dirent.h>
#include <errno.h>
#include <sys/stat.h>
#include <libgen.h>
#include <fcntl.h>

// Dichiarazioni di variabili
#define BUFFER_SIZE 1024
#define END_OF_LIST "END_OF_LIST"

// Prototipi di funzione
void error_handling(char *message);
void send_file(int sock, const char *local_path, const char *remote_path);
void receive_file(int sock, const char *remote_path, const char *local_path);
void list_files(int sock, const char *remote_path);
int create_full_path(const char *path);

