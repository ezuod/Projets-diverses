#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <string.h>

#define BUFFER_SIZE 4

int tailFile(int inputFD, int outputFD, int numLines) {
    struct stat fileStat;

    if (fstat(inputFD, &fileStat) == -1) {
        fprintf(stderr,"Erreur fichier");
        return 1;
    }

    if (S_ISREG(fileStat.st_mode)) {
        // Fichier régulier
        off_t fileSize = lseek(inputFD, 0, SEEK_END);
        if (fileSize == -1) {
            fprintf(stderr,"Erreur lseek sur le fichier");
            return 1;}

        int lineCount = 0;
        off_t position = fileSize;
        char buffer[BUFFER_SIZE];
        ssize_t bytesRead;

        // Lire de la fin au le début
        while (position > 0 && lineCount <= numLines) {
            ssize_t bytesToRead = (position >= BUFFER_SIZE) ? BUFFER_SIZE : position;
            position = lseek(inputFD, -bytesToRead, SEEK_CUR); 
            bytesRead = read(inputFD, buffer, bytesToRead);
            if (bytesRead == -1) {
                fprintf(stderr,"Erreur read sur le fichier");
                return 1;}
            // Compter les nouvelles lignes trouvées
            for (ssize_t i = bytesRead - 1; i >= 0; i--) {
                if (buffer[i] == '\n') {
                    lineCount++;
                    if (lineCount > numLines) {
                        position += i + 1;
                        break;}
                }
            }
            if (lineCount > numLines) {break;}
            position = lseek(inputFD, -bytesRead, SEEK_CUR);  //retour
        }

        lseek(inputFD, position, SEEK_SET);
        while ((bytesRead = read(inputFD, buffer, BUFFER_SIZE)) > 0) {
            write(outputFD, buffer, bytesRead);
        }

    } else {
        //Fichier pas régulier
        char **lines = malloc(sizeof(char*) * numLines); 
        for (int i = 0; i < numLines; i++) {
            lines[i] = NULL;}
        int currentLine = 0;
        int totalLines = 0;
        char buffer[BUFFER_SIZE];
        ssize_t bytesRead;
        ssize_t lineLength = 0;
        char *lineBuffer = NULL;
        //Lire le fichier caractère par caractère
        while ((bytesRead = read(inputFD, buffer, BUFFER_SIZE)) > 0) {
            for (ssize_t i = 0; i < bytesRead; i++) {
                lineBuffer = realloc(lineBuffer, lineLength + 1);
                lineBuffer[lineLength++] = buffer[i];
                if (buffer[i] == '\n') {
                    lineBuffer[lineLength] = '\0';
                    free(lines[currentLine]); 
                    lines[currentLine] = lineBuffer;
                    currentLine = (currentLine + 1) % numLines;
                    totalLines++;

                    lineBuffer = NULL;
                    lineLength = 0;}}
        }

        if (lineLength > 0) {
            lineBuffer = realloc(lineBuffer, lineLength + 1);
            lineBuffer[lineLength] = '\0'; 
            free(lines[currentLine]);
            lines[currentLine] = lineBuffer;
            currentLine = (currentLine + 1) % numLines;
            totalLines++;
        }

        //Afficher les dernières lignes
        int start;
        if (totalLines < numLines) {
            start = 0;}
        else {
            start = currentLine;}

        int count;
        if (totalLines < numLines) {
            count = totalLines;} 
        else {
            count = numLines;}
    
        for (int i = 0; i < count; i++) {
            int index = (start + i) % numLines;
            if (lines[index]) {
                write(outputFD, lines[index], strlen(lines[index]));
            }
        }

        //Nettoyage de la mémoire allouée
        for (int i = 0; i < numLines; i++) {
            free(lines[i]);
        }
        free(lines);
    }

    return 0;
}

