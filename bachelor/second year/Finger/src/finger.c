#include "finger.h"

// Funzione per verificare se un dato username esiste nel sistema
bool is_username(char *username){
  struct passwd *pwd = getpwnam(username);
  if (pwd != NULL ) 
  {
    return true;
  } 
  return false;
}

// Funzione per ottenere lo username associato a un nome reale
// Restituisce il primo username trovato con quel nome reale
char *get_username_by_realname(const char *real_name) {
  struct passwd *pw_entry;
  setpwent();
  while ((pw_entry = getpwent()) != NULL) {
    char gecos_copy[strlen(pw_entry->pw_gecos) + 1];
    strcpy(gecos_copy, pw_entry->pw_gecos);
    char *token = strtok(gecos_copy, ",");
    if (token != NULL) {
      char *space_token = strtok(gecos_copy, " ");
      while (space_token != NULL) {
        if (strcmp(space_token, real_name) == 0) {
          endpwent();
          return pw_entry->pw_name;
        }
        space_token = strtok(NULL, " ");
      }
    }
  }
  endpwent();
  return "";
}

// Funzione per ottenere tutti gli username associati a un nome reale
char** get_usernames_by_realname(const char *real_name, int *count) {
  struct passwd *pw_entry;
  int user_count = 0;
  int user_capacity = 10;
  char **usernames = malloc(user_capacity * sizeof(char*));
  setpwent(); // Inizializza la lettura del file delle password
  while ((pw_entry = getpwent()) != NULL) {
    char gecos_copy[strlen(pw_entry->pw_gecos) + 1];
    strcpy(gecos_copy, pw_entry->pw_gecos);
    char *token = strtok(gecos_copy, ",");
    if (token != NULL) {
      char *space_token = strtok(gecos_copy, " ");
      while (space_token != NULL) {
        if (strcmp(space_token, real_name) == 0) {
          if (user_count >= user_capacity) {
            user_capacity *= 2;
            usernames = realloc(usernames, user_capacity * sizeof(char*));
          }
          usernames[user_count] = strdup(pw_entry->pw_name);
          user_count++;
          break;
        }
        space_token = strtok(NULL, " ");
      }
    }
  }
  endpwent(); // Termina la lettura del file delle password
  *count = user_count;
  return usernames;
}

// Funzione per formattare un numero di telefono in diversi formati
char* format_phone_number(const char* phone_number) {
  int len = strlen(phone_number);
  char* formatted_number = (char*)malloc(20 * sizeof(char));
  strcpy(formatted_number, "");
  
  if (len == 0) {
    return formatted_number;
  }
  if (len == 11) { // Formato: +N-NNN-NNN-NNNN
    sprintf(formatted_number, "+%c-%c%c%c-%c%c%c-%c%c%c%c",
            phone_number[0], phone_number[1], phone_number[2], phone_number[3],
                                                                           phone_number[4], phone_number[5], phone_number[6],phone_number[7],
                                                                                                                                         phone_number[8], phone_number[9], phone_number[10]);
  } else if (len == 10) { // Formato: NNN-NNN-NNNN
    sprintf(formatted_number, "%c%c%c-%c%c%c-%c%c%c%c",
            phone_number[0], phone_number[1], phone_number[2],
                                                          phone_number[3], phone_number[4], phone_number[5],
                                                                                                        phone_number[6], phone_number[7], phone_number[8], phone_number[9]);
  } else if (len == 7){ // Formato: NNN-NNNN
    sprintf(formatted_number, "%c%c%c-%c%c%c%c",
            phone_number[0], phone_number[1], phone_number[2],
                                                          phone_number[3], phone_number[4], phone_number[5],
                                                                                                        phone_number[6]);
  } else if (len == 5) { // Formato: xN-NNNN
    sprintf(formatted_number, "x%c-%c%c%c%c",
            phone_number[0], phone_number[1], phone_number[2], phone_number[3], phone_number[4]);
  } else if (len == 4) { // Formato: xNNNN
    sprintf(formatted_number, "x%c%c%c%c",
            phone_number[0], phone_number[1], phone_number[2], phone_number[3]);
  } else {
    return formatted_number;
  }
  return formatted_number;
}

// Funzione per ottenere le informazioni dell'utente dal file delle password
void get_pwd_info(const char *username, UserInfo *user_info, bool is_real_name) {
  struct passwd *pw_entry = getpwnam(username);
  if (pw_entry == NULL)  {
    // Se l'utente non è trovato, cerchiamo nel real name
    char *username_by_realname = get_username_by_realname(username);
    if ((strcmp(username_by_realname, "") == 0) || !is_real_name) {
      // Se il real name non è trovato, restituiamo una struttura UserInfo vuota
      memset(user_info, 0, sizeof(UserInfo));
      return;
    }
    pw_entry = getpwnam(username_by_realname);
  }
  // Copiamo le informazioni ottenute nella struttura UserInfo
  strcpy(user_info->username, pw_entry->pw_name);
  
  char *token;
  char gecos_copy[strlen(pw_entry->pw_gecos) + 1];
  strcpy(gecos_copy, pw_entry->pw_gecos);
  token = strtok(gecos_copy, ",");
  if (token != NULL) {
    strcpy(user_info->real_name, token);
    token = strtok(NULL, ",");
  } else {
    strcpy(user_info->real_name, "");
  }
  if (token != NULL) {
    strcpy(user_info->office_location, token);
    token = strtok(NULL, ",");
  } else {
    strcpy(user_info->office_location, "*");
  }
  if (token != NULL) {
    strcpy(user_info->office_phone, format_phone_number(token));
  } else {
    strcpy(user_info->office_phone, "*");
  }
  if (token != NULL) {
    strcpy(user_info->home_phone, format_phone_number(token));
  } else {
    strcpy(user_info->home_phone, "*");
  }
  
  strcpy(user_info->home_directory, pw_entry->pw_dir);
  strcpy(user_info->login_shell, pw_entry->pw_shell);
}

// Funzione per copiare le informazioni dal file utmp nella struttura UserInfo
void copy_utmp_info(struct utmp *ut, UserInfo *user_info) {
  time_t  now;
  time(&now);
  // Calcola il tempo di inattività
  int idle_hours = (now - ut->ut_tv.tv_sec) / 3600;
  int idle_minutes = (now - ut->ut_tv.tv_sec) / 60 %60;
  sprintf(user_info->idle_time, "%d:%02d", idle_hours, idle_minutes);
  strcpy(user_info->write_status, *ut->ut_name ? "" : "*");
  strcpy(user_info->terminal, ut->ut_line);
  // Calcola l'orario di login
  time_t login_timestamp = ut->ut_tv.tv_sec;
  struct tm *login_time_info = localtime(&login_timestamp);
  time_t six_months_ago = now - (6 * 30 * 24 * 60 * 60);
  if (login_timestamp < six_months_ago) {
    strftime(user_info->login_time, sizeof(user_info->login_time), "%b %d %Y", login_time_info);
  } else {
    strftime(user_info->login_time, sizeof(user_info->login_time), "%b %d %H:%M", login_time_info);
  }
}

// Funzione per ottenere le informazioni dal file utmp
void get_utmp_info(const char *username, struct utmp *ut, UserInfo *user_info) {
  if (ut == NULL) {
    struct utmp *ut_entry;
    int user_found = 0;
    setutent();
    while ((ut_entry = getutent()) != NULL) {
      if (strcmp(ut_entry->ut_user, username) == 0) {
        copy_utmp_info(ut_entry, user_info);
        user_found = 1;
        break;
      }
    }
    endutent();
    if (!user_found) {
      strcpy(user_info->write_status, "");
      strcpy(user_info->terminal, "*");
      strcpy(user_info->login_time, "*");
      strcpy(user_info->idle_time, "*");
    }
  } else {
    copy_utmp_info(ut, user_info);
  }
}

// Funzione principale per ottenere le informazioni complete di un utente specificato
UserInfo get_user_info(const char *username) {
  UserInfo user_info;
  get_pwd_info(username, &user_info, true);
  get_utmp_info(username, NULL, &user_info);
  
  return user_info;
}

// Funzione per ottenere le informazioni di un utente specificato senza considerare il suo nome reale
UserInfo get_user_info_noreal(const char *username) {
  UserInfo user_info;
  get_pwd_info(username, &user_info, false);
  get_utmp_info(username, NULL, &user_info);
  
  return user_info;
}

// Ottiene le informazioni dell'utente dato lo username e una voce del file utmp
UserInfo get_single_user_info(const char *username, struct utmp *ut) {
  UserInfo user_info;
  get_pwd_info(username, &user_info, true);
  get_utmp_info(username, ut, &user_info);
  
  return user_info;
}

// Stampa le informazioni brevi di un utente
void print_short_info(const UserInfo *user_info) {
  // Costruzione della stringa formattata
  char body[512];
  snprintf(body, 512, "%-10s%-20s%-6s%s%-6s%-15s%-10s%-10s\n",
           user_info->username,
           user_info->real_name,
           user_info->terminal, user_info->write_status,
           user_info->idle_time,
           user_info->login_time,
           user_info->office_location,
           user_info->office_phone);
  printf("%s", body);
}

// Stampa l'intestazione per le informazioni brevi
void print_short_header() {
  char header[512];
  snprintf(header, 512, "%-10s%-20s%-6s%-6s%-15s%-10s%-10s\n", "Login", "Name", "Tty", "Idle", "Login Time", "Office", "Office Phone");
  printf("%s", header);
}

// Stampa le informazioni brevi di un utente specificato
void print_short_user(const char *username) {
  UserInfo user_info = get_user_info(username);
  if (strlen(user_info.username) > 0) {
    print_short_header();
    print_short_info(&user_info);
  } else {
    printf("finger: %s no such users\n", username);
  }
}

// Stampa le informazioni brevi di un utente specificato senza l'intestazione
void print_short_user_without_header(const char *username) {
  UserInfo user_info = get_user_info(username);
  if (strlen(user_info.username) > 0) {
    print_short_info(&user_info);
  } else {
    printf("finger: %s no such users\n", username);
  }
}

// Stampa le informazioni lunghe di un utente
void print_long_info(const UserInfo *user_info) {
  printf("Login: %-24sName: %-20s\n", user_info->username, user_info->real_name);
  printf("Directory: %-20sShell: %-20s\n", user_info->home_directory, user_info->login_shell);
  printf("Office Phone: %-16s", user_info->office_phone);
  printf("Home Phone: %-16s\n", user_info->home_phone);
  printf("On since %s (UTC) on %s from %s\n", user_info->login_time, user_info->terminal, user_info->terminal);
  printf(" %s idle\n", user_info->idle_time);
  printf("\n");
}

// Stampa le informazioni lunghe di un utente specificato
void print_long_user(const char *username) {
  UserInfo user_info = get_user_info(username);
  if (strlen(user_info.username) > 0) {
    print_long_info(&user_info);
  } else {
    printf("finger: %s no such users\n", username);
  }
}

// Stampa le informazioni di un utente in base al formato richiesto (breve o lungo)
void print_m_user(const char *username, bool is_short) {
  UserInfo user_info = get_user_info_noreal(username);
  if (strlen(user_info.username) > 0) {
    if (is_short) {
      print_short_header();
      print_short_info(&user_info);
    } else {
      print_long_info(&user_info);
    }
  } else {
    printf("finger: %s no such users\n", username);
  }
}

// Stampa le informazioni di un utente senza intestazione in base al formato richiesto (breve o lungo)
void print_m_user_without_header(const char *username, bool is_short) {
  UserInfo user_info = get_user_info_noreal(username);
  if (strlen(user_info.username) > 0) {
    if (is_short) {
      print_short_info(&user_info);
    } else {
      print_long_info(&user_info);
    }
  } else {
    printf("finger: %s no such users\n", username);
  }
}

// Stampa le informazioni di tutti gli utenti connessi
void print_connected_users(int is_short) {
  struct utmp *ut;
  setutent();
  if (is_short) {
    print_short_header();
  }
  while ((ut = getutent()) != NULL) {
    if (ut->ut_type == USER_PROCESS) {
      char *username = ut->ut_user;
      UserInfo user_info = get_single_user_info(username, ut);
      if (is_short) {
        print_short_info(&user_info);
      } else {
        print_long_info(&user_info);
      }
    }
  }
  endutent();
}

// Ottiene una lista ordinata di utenti unici
char **get_sorted_unique_users(char *users[], int start_index, int last_index, bool is_realname) {
  int capacity = (last_index - start_index);
  char **exists_users_list = malloc(capacity * sizeof(char*));
  char **not_exists_users_list = malloc(capacity * sizeof(char*));
  int exist_index = 0;
  int not_exist_index = 0;
  
  for (int i = start_index; i < last_index; ++i) {
    UserInfo user_info;
    if (is_realname) {
      user_info = get_user_info(users[i]);
    } else {
      user_info = get_user_info_noreal(users[i]);
    }
    if (strlen(user_info.username) > 0) {
      bool is_duplicate = false;
      for (int k = 0; k < exist_index; ++k) {
        if (strcmp(exists_users_list[k], user_info.username) == 0) {
          is_duplicate = true;
          break;
        }
      }
      if (!is_duplicate) {
        exists_users_list[exist_index++] = strdup(user_info.username);
      }
      // Aggiungi anche tutti gli username degli utenti con quel realname
      if (is_realname) {
        int count;
        char **realname_usernames = get_usernames_by_realname(users[i], &count);
        for (int j = 0; j < count; ++j) {
          is_duplicate = false;
          for (int k = 0; k < exist_index; ++k) {
            if (strcmp(exists_users_list[k], realname_usernames[j]) == 0) {
              is_duplicate = true;
              break;
            }
          }
          if (!is_duplicate) {
            if (exist_index >= capacity) {
              capacity += count - j + 1;
              exists_users_list = realloc(exists_users_list, capacity * sizeof(char*));
            }
            exists_users_list[exist_index++] = strdup(realname_usernames[j]);
          }
        }
        for (int j = 0; j < count; ++j) {
          free(realname_usernames[j]);
        }
        free(realname_usernames);
      }
    } else {
      not_exists_users_list[not_exist_index++] = strdup(users[i]);
    }
  }
  int total_users = exist_index + not_exist_index;
  static char *all_users_list[MAX_USERS];
  int index = 0;
  for (int i = 0; i < not_exist_index; ++i) {
    all_users_list[index++] = not_exists_users_list[i];
  }
  for (int i = 0; i < exist_index; ++i) {
    all_users_list[index++] = exists_users_list[i];
  }
  all_users_list[index++] = NULL;
  return all_users_list;
}

// Verifica se l'opzione passata è valida
bool is_valid_option(const char *option) {
  for(int i=1; i<strlen(option); i++) {
    if (!strchr("lms", option[i])) {
      printf("finger: invalid option -- '%c'\n", option[i]);
      printf("usage: finger [-lms] [login ...]\n");
      return false;
    }
  }
  return true;
}

// Funzione principale del programma
int main(int argc, char *argv[]) {
  if (argc == 1) {
    print_connected_users(1);
  } else if (argc == 2) {
    if (argv[1][0] != '-') {
      char **sorted_unique_users = get_sorted_unique_users(argv, 1, argc, true);
      int num_users = 0;
      while (sorted_unique_users[num_users] != NULL) {
        num_users++;
      }
      for (int i = 0; i < num_users; ++i) {
        print_long_user(sorted_unique_users[i]);
      }
    } else {
      if (!is_valid_option(argv[1])) {
        return 1;  
      } else {    
        if (((strchr(argv[1], 's')) || (strchr(argv[1], 'm'))) && (!strchr(argv[1], 'l'))) {
          print_connected_users(1);
        } else {
          print_connected_users(0);
        }
      }
    }  
  } else if ((argc > 2) && (argv[1][0] == '-')){
    if (!is_valid_option(argv[1])) {
      return 1;
    }
    char **sorted_unique_users = get_sorted_unique_users(argv, 2, argc, true);
    int num_users = 0;
    while (sorted_unique_users[num_users] != NULL) {
      num_users++;
    }
    if (strchr(argv[1], 'm')) {
      char **sorted_unique_users = get_sorted_unique_users(argv, 2, argc, false);
      int num_users = 0;
      while (sorted_unique_users[num_users] != NULL) {
        num_users++;
      }
      if ((strchr(argv[1], 's')) && (!strchr(argv[1], 'l'))) {
        bool is_header = false;
        for (int i = 0; i < num_users; ++i) {
          if ((is_username(sorted_unique_users[i])) && !is_header) {
            is_header = true;
            print_m_user(sorted_unique_users[i], true);
          } else {
            print_m_user_without_header(sorted_unique_users[i], true); 
          }
        }
      } else {
        for (int i = 0; i < num_users; ++i) {
          print_m_user_without_header(sorted_unique_users[i], false);
        }
      }
    } else if (strchr(argv[1], 'l')) {
      for (int i=0; i < num_users; ++i) {
        print_long_user(sorted_unique_users[i]);
      }
    } else if (strchr(argv[1], 's')) {
      bool is_header = false;
      for (int i = 0; i < num_users; ++i) {
        if ((is_username(sorted_unique_users[i])) && !is_header) {
          is_header = true;
          print_short_user(sorted_unique_users[i]);
        } else {
          print_short_user_without_header(sorted_unique_users[i]); 
        }
      }
    } else {
      printf("%s\n", argv[1]);
    }
  } else if (argc > 2) {
    char **sorted_unique_users = get_sorted_unique_users(argv, 1, argc, true);
    int num_users = 0;
    while (sorted_unique_users[num_users] != NULL) {
      num_users++;
    }
    for (int i=0; i < num_users; ++i) {
      print_long_user(sorted_unique_users[i]);
    }
  } else {
    printf("usage: finger [-lms] [login ...]\n");
  }
  
  return 0;
}
