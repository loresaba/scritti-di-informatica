// Definizione di costanti
#define MAX_USERNAME_LENGTH 50
#define MAX_HOSTNAME_LENGTH 100
#define MAX_PATH_LENGTH 256
#define MAX_FILE_CONTENT_LENGTH 1024
#define MAX_USERS 50

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pwd.h>
#include <utmp.h>
#include <time.h>
#include <stdbool.h>

// Struttura per memorizzare le informazioni di un utente
typedef struct {
  char username[MAX_USERNAME_LENGTH];
  char real_name[MAX_USERNAME_LENGTH];
  char terminal[MAX_USERNAME_LENGTH];
  char write_status[2];
  char idle_time[10];
  char login_time[20];
  char office_location[MAX_PATH_LENGTH];
  char office_phone[20];
  char home_directory[MAX_PATH_LENGTH];
  char home_phone[20];
  char login_shell[MAX_PATH_LENGTH];
} UserInfo;

// Prototipi di funzione
bool is_username(char *username);
char *get_username_by_realname(const char *real_name);
char** get_usernames_by_realname(const char *real_name, int *count);
char* format_phone_number(const char* phone_number);
void get_pwd_info(const char *username, UserInfo *user_info, bool is_real_name);
void get_utmp_info(const char *username, struct utmp *ut, UserInfo *user_info);
void copy_utmp_info(struct utmp *ut, UserInfo *user_info);
UserInfo get_user_info(const char *username);
UserInfo get_user_info_noreal(const char *username);
UserInfo get_single_user_info(const char *username, struct utmp *ut);
void print_short_info(const UserInfo *user_info);
void print_short_header();
void print_short_user(const char *username);
void print_short_user_without_header(const char *username);
void print_long_info(const UserInfo *user_info);
void print_long_user(const char *username);
void print_m_user(const char *username, bool is_short);
void print_m_user_without_header(const char *username, bool is_short);
void print_connected_users(int is_short);
char **get_sorted_unique_users(char *users[], int start_index, int last_index, bool is_realname);
bool is_valid_option(const char *option);