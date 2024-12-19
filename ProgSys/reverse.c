#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>

void afficher_ligne (int fd, int max_cars)
{
  char c;
  for (int i = 0; i < max_cars; i++) {
    read (fd, &c, 1);
    write (1, &c, 1);
  }
  if (c != '\n')
    write (1, "\n", 1);
}

int main (int argc, char *argv [])
{
  if (argc < 3) {
    fprintf (stderr, "Usage: %s <file> <index_file\n", argv [0]);
    exit (1);
  }

  int fd_file = open (argv [1], O_RDONLY);
  if (fd_file == -1) {
    perror ("open");
    exit (1);
  }

  int fd_index = open (argv [2], O_RDONLY);
  if (fd_index == -1) {
    perror ("open");
    exit (1);
  }

  int fin, nb_lignes;

  fin = lseek (fd_file, 0, SEEK_END);
  nb_lignes = lseek (fd_index, 0, SEEK_END) / sizeof (int);

  for (int ligne = nb_lignes - 1; ligne >= 0; ligne--) {
    int debut;
    lseek (fd_index, ligne * sizeof (int), SEEK_SET);
    read (fd_index, &debut, sizeof (int));
    lseek (fd_file, debut, SEEK_SET);
    afficher_ligne (fd_file, fin - debut);
    fin = debut;
  }
    
  return 0;
}