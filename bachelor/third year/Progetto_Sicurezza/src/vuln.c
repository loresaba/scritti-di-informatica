#include <stdio.h>
#include <string.h>

void vulnerable() {
    char buffer[100];
    printf("Inserisci input: ");
    //Questa funzione non è sicura perchè non controlla la lunghezza dell'input
    gets(buffer);  
}

int main() {
    vulnerable();
   
}
