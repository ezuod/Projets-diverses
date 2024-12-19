#include "Evaluation.h"
#include "Shell.h"
#include <fcntl.h>
#include <signal.h>
#include <stdio.h>
#include <string.h>
#include <sys/wait.h>
#include <unistd.h>
#include <stdlib.h>

void handle_sigchld(int sig) {
    (void)sig; 
    int saved_errno = errno; 
    while (waitpid(-1, NULL, WNOHANG) > 0);
    errno = saved_errno; 
}

int executeCommand(Expression *expr, int background) {
    pid_t pid = fork();
    if (pid == 0) {
        if (expr->argc > 0) {
            execvp(expr->argv[0], expr->argv);
            perror("Erreur : execvp");
            _exit(127);
        }
    } else if (pid < 0) {
        perror("Erreur : fork");
        return 1;
    } else {
        if (background) {
            printf("Commande en arrière-plan, PID : %d\n", pid);
        } else {
            int status;
            waitpid(pid, &status, 0);
            return WEXITSTATUS(status);
        }
    }
    return 0;
}

int evaluateExpr(Expression *expr) {
    if (expr == NULL) {
        return 1;
    }

    int status = 0;

    switch (expr->type) {
        case ET_SIMPLE: {
            int background = 0;
            if (expr->argc > 0 && strcmp(expr->argv[expr->argc - 1], "&") == 0) {
                background = 1;
                expr->argv[expr->argc - 1] = NULL; 
                expr->argc--;
            }

            status = executeCommand(expr, background);
            break;
        }

        case ET_SEQUENCE:
            evaluateExpr(expr->left);
            evaluateExpr(expr->right);
            break;

        case ET_SEQUENCE_AND:
            if (evaluateExpr(expr->left) == 0)
                status = evaluateExpr(expr->right);
            break;

        case ET_SEQUENCE_OR:
            if (evaluateExpr(expr->left) != 0)
                status = evaluateExpr(expr->right);
            break;

        case ET_PIPE:
            status = executePipeline(expr);
            break;

        default:
            fprintf(stderr, "Erreur : expression inconnue\n");
            status = 1;
    }

    return status;
}

int main(int argc, char *argv[]) {
    (void)argc; 
    (void)argv;
    struct sigaction sa;
    sa.sa_handler = handle_sigchld;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = SA_RESTART | SA_NOCLDSTOP; // Redémarrer les appels interrompus, ignorer les enfants arrêtés
    if (sigaction(SIGCHLD, &sa, NULL) == -1) {
        perror("Erreur : sigaction");
        exit(1);
    }

    // Boucle principale (exemple d'utilisation)
    while (1) {
        char command[1024];
        printf("shell> ");
        if (fgets(command, sizeof(command), stdin) == NULL) {
            break;
        }

        // Simuler une expression pour tester
        Expression expr = { .type = ET_SIMPLE, .argc = 1, .argv = {strtok(command, "\n")} };
        evaluateExpr(&expr);
    }

    return 0;
}
