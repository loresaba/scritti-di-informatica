#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <pthread.h>
#include <sys/stat.h>
#include <dirent.h>
#include <errno.h>
#include <libgen.h>
#include <fcntl.h>

// Dichiarazioni di variabili
#define BUFFER_SIZE 1024
#define END_OF_LIST "END_OF_LIST"

char *ft_root_directory;
pthread_mutex_t file_mutex = PTHREAD_MUTEX_INITIALIZER;

// Prototipi di funzione
void *handle_client(void *arg);
void send_file_list(int client_sock, const char *path);
void send_file(int client_sock, const char *path);
void receive_file(int client_sock, const char *path);
void error_handling(char *message);
int create_full_path(const char *path);
