#!/usr/bin/env python3
"""
HEGEMONIA - Launcher Simple
Launcher minimal pour se connecter rapidement au serveur
"""

import tkinter as tk
from tkinter import ttk, messagebox
import subprocess
import os
import sys
import json
from pathlib import Path
import urllib.request
import zipfile
import shutil

class HegemoniaLauncher:
    def __init__(self):
        self.window = tk.Tk()
        self.window.title("Hegemonia Launcher")
        self.window.geometry("600x400")
        self.window.resizable(False, False)

        # Configuration
        self.server_ip = "VOTRE_IP_VPS"  # À remplacer
        self.server_port = "25577"
        self.minecraft_version = "1.20.4"

        # Chemins
        self.launcher_dir = Path.home() / ".hegemonia"
        self.minecraft_dir = self.launcher_dir / "minecraft"
        self.launcher_dir.mkdir(exist_ok=True)

        self.setup_ui()

    def setup_ui(self):
        """Configure l'interface utilisateur"""
        # Style
        style = ttk.Style()
        style.theme_use('clam')

        # Header
        header = tk.Frame(self.window, bg="#2b2d31", height=80)
        header.pack(fill=tk.X)

        title = tk.Label(
            header,
            text="HEGEMONIA",
            font=("Arial", 24, "bold"),
            fg="#f0b90b",
            bg="#2b2d31"
        )
        title.pack(pady=20)

        # Main content
        content = tk.Frame(self.window, bg="#36393f")
        content.pack(fill=tk.BOTH, expand=True, padx=20, pady=20)

        # Server info
        info_frame = tk.Frame(content, bg="#36393f")
        info_frame.pack(pady=10)

        tk.Label(
            info_frame,
            text=f"Serveur: {self.server_ip}:{self.server_port}",
            font=("Arial", 12),
            fg="white",
            bg="#36393f"
        ).pack()

        tk.Label(
            info_frame,
            text=f"Version: Minecraft {self.minecraft_version}",
            font=("Arial", 10),
            fg="#b9bbbe",
            bg="#36393f"
        ).pack()

        # Pseudo input
        pseudo_frame = tk.Frame(content, bg="#36393f")
        pseudo_frame.pack(pady=20)

        tk.Label(
            pseudo_frame,
            text="Pseudo:",
            font=("Arial", 11),
            fg="white",
            bg="#36393f"
        ).pack(side=tk.LEFT, padx=5)

        self.pseudo_entry = tk.Entry(
            pseudo_frame,
            font=("Arial", 11),
            width=20,
            bg="#40444b",
            fg="white",
            insertbackground="white"
        )
        self.pseudo_entry.pack(side=tk.LEFT, padx=5)
        self.pseudo_entry.insert(0, "Joueur")

        # Progress bar
        self.progress = ttk.Progressbar(
            content,
            mode='indeterminate',
            length=300
        )
        self.progress.pack(pady=10)

        # Status label
        self.status_label = tk.Label(
            content,
            text="Prêt à jouer",
            font=("Arial", 10),
            fg="#b9bbbe",
            bg="#36393f"
        )
        self.status_label.pack(pady=5)

        # Launch button
        self.launch_btn = tk.Button(
            content,
            text="▶ JOUER",
            font=("Arial", 14, "bold"),
            bg="#5865f2",
            fg="white",
            activebackground="#4752c4",
            activeforeground="white",
            relief=tk.FLAT,
            cursor="hand2",
            command=self.launch_game,
            width=20,
            height=2
        )
        self.launch_btn.pack(pady=20)

        # Footer
        footer = tk.Label(
            self.window,
            text="Hegemonia - Serveur Géopolitique Minecraft",
            font=("Arial", 8),
            fg="#72767d",
            bg="#2b2d31"
        )
        footer.pack(side=tk.BOTTOM, fill=tk.X, pady=5)

    def update_status(self, message):
        """Met à jour le message de statut"""
        self.status_label.config(text=message)
        self.window.update()

    def launch_game(self):
        """Lance Minecraft et se connecte au serveur"""
        pseudo = self.pseudo_entry.get().strip()

        if not pseudo:
            messagebox.showerror("Erreur", "Veuillez entrer un pseudo")
            return

        if len(pseudo) < 3 or len(pseudo) > 16:
            messagebox.showerror("Erreur", "Le pseudo doit faire entre 3 et 16 caractères")
            return

        self.launch_btn.config(state=tk.DISABLED)
        self.progress.start()

        try:
            self.update_status("Vérification de Minecraft...")

            # Vérifier si Minecraft est installé
            if not self.check_minecraft():
                self.update_status("Minecraft non trouvé - Veuillez l'installer")
                messagebox.showinfo(
                    "Minecraft requis",
                    f"Veuillez installer Minecraft {self.minecraft_version} via le launcher officiel,\n"
                    f"puis réessayez."
                )
                return

            self.update_status("Lancement de Minecraft...")

            # Lancer Minecraft
            self.start_minecraft(pseudo)

            self.update_status("Minecraft lancé ! Bon jeu !")

            # Fermer le launcher après 2 secondes
            self.window.after(2000, self.window.destroy)

        except Exception as e:
            messagebox.showerror("Erreur", f"Erreur lors du lancement:\n{str(e)}")
            self.update_status("Erreur - Réessayez")
        finally:
            self.progress.stop()
            self.launch_btn.config(state=tk.NORMAL)

    def check_minecraft(self):
        """Vérifie si Minecraft est installé"""
        # Chemins courants du launcher Minecraft
        if sys.platform == "win32":
            minecraft_path = Path(os.getenv("APPDATA")) / ".minecraft"
        elif sys.platform == "darwin":
            minecraft_path = Path.home() / "Library" / "Application Support" / "minecraft"
        else:  # Linux
            minecraft_path = Path.home() / ".minecraft"

        return minecraft_path.exists()

    def start_minecraft(self, pseudo):
        """Lance Minecraft avec les paramètres pour se connecter au serveur"""

        if sys.platform == "win32":
            # Windows
            minecraft_exe = Path(os.getenv("APPDATA")) / ".minecraft" / "launcher" / "MinecraftLauncher.exe"
            if minecraft_exe.exists():
                subprocess.Popen([
                    str(minecraft_exe),
                    f"--server {self.server_ip}",
                    f"--port {self.server_port}",
                    f"--username {pseudo}"
                ])
            else:
                # Fallback: ouvrir le launcher normal
                os.startfile("minecraft://")
                messagebox.showinfo(
                    "Connexion manuelle",
                    f"Veuillez vous connecter manuellement à:\n{self.server_ip}:{self.server_port}"
                )

        elif sys.platform == "darwin":
            # macOS
            subprocess.Popen([
                "open",
                "-a",
                "Minecraft",
                "--args",
                f"--server {self.server_ip}",
                f"--port {self.server_port}"
            ])

        else:
            # Linux
            messagebox.showinfo(
                "Connexion manuelle",
                f"Lancez Minecraft et connectez-vous à:\n{self.server_ip}:{self.server_port}\n\nPseudo: {pseudo}"
            )

    def run(self):
        """Lance le launcher"""
        self.window.mainloop()

if __name__ == "__main__":
    launcher = HegemoniaLauncher()
    launcher.run()
