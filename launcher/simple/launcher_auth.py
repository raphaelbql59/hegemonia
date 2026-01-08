#!/usr/bin/env python3
"""
HEGEMONIA - Launcher avec Authentification
Launcher professionnel pour le serveur Minecraft geopolitique Hegemonia
"""

import tkinter as tk
from tkinter import ttk, messagebox
import subprocess
import os
import sys
import json
from pathlib import Path
import urllib.request
import urllib.parse
import ssl

class HegemoniaLauncher:
    def __init__(self):
        self.window = tk.Tk()
        self.window.title("Hegemonia Launcher")
        self.window.geometry("800x600")
        self.window.resizable(False, False)
        self.window.configure(bg="#1a1b1e")

        # Configuration serveur
        self.api_url = "http://51.75.31.173:3001"
        self.server_ip = "51.75.31.173"
        self.server_port = "25577"
        self.minecraft_version = "1.20.4"

        # Etat
        self.token = None
        self.user = None
        self.config_file = Path.home() / ".hegemonia" / "config.json"
        self.config_file.parent.mkdir(exist_ok=True)

        # Charger config sauvegardee
        self.load_config()

        # Afficher la bonne page
        if self.token and self.user:
            self.setup_dashboard()
        else:
            self.setup_login()

    def load_config(self):
        """Charge la configuration sauvegardee"""
        try:
            if self.config_file.exists():
                with open(self.config_file, 'r') as f:
                    config = json.load(f)
                    self.token = config.get('token')
                    self.user = config.get('user')
        except:
            pass

    def save_config(self):
        """Sauvegarde la configuration"""
        try:
            with open(self.config_file, 'w') as f:
                json.dump({
                    'token': self.token,
                    'user': self.user
                }, f)
        except:
            pass

    def clear_window(self):
        """Efface tous les widgets de la fenetre"""
        for widget in self.window.winfo_children():
            widget.destroy()

    def setup_login(self):
        """Interface de connexion"""
        self.clear_window()

        # Container principal
        main = tk.Frame(self.window, bg="#1a1b1e")
        main.pack(fill=tk.BOTH, expand=True)

        # Logo/Titre
        title_frame = tk.Frame(main, bg="#1a1b1e")
        title_frame.pack(pady=60)

        tk.Label(
            title_frame,
            text="HEGEMONIA",
            font=("Arial", 36, "bold"),
            fg="#f0b90b",
            bg="#1a1b1e"
        ).pack()

        tk.Label(
            title_frame,
            text="Serveur Geopolitique Minecraft",
            font=("Arial", 12),
            fg="#72767d",
            bg="#1a1b1e"
        ).pack(pady=5)

        # Formulaire de connexion
        form_frame = tk.Frame(main, bg="#2b2d31", padx=40, pady=30)
        form_frame.pack(pady=20)

        tk.Label(
            form_frame,
            text="Connexion",
            font=("Arial", 18, "bold"),
            fg="white",
            bg="#2b2d31"
        ).pack(pady=(0, 20))

        # Email
        tk.Label(
            form_frame,
            text="Email",
            font=("Arial", 10),
            fg="#b9bbbe",
            bg="#2b2d31",
            anchor="w"
        ).pack(fill=tk.X)

        self.email_entry = tk.Entry(
            form_frame,
            font=("Arial", 12),
            width=35,
            bg="#40444b",
            fg="white",
            insertbackground="white",
            relief=tk.FLAT
        )
        self.email_entry.pack(pady=(5, 15), ipady=8)

        # Mot de passe
        tk.Label(
            form_frame,
            text="Mot de passe",
            font=("Arial", 10),
            fg="#b9bbbe",
            bg="#2b2d31",
            anchor="w"
        ).pack(fill=tk.X)

        self.password_entry = tk.Entry(
            form_frame,
            font=("Arial", 12),
            width=35,
            bg="#40444b",
            fg="white",
            insertbackground="white",
            relief=tk.FLAT,
            show="*"
        )
        self.password_entry.pack(pady=(5, 20), ipady=8)

        # Message d'erreur
        self.error_label = tk.Label(
            form_frame,
            text="",
            font=("Arial", 10),
            fg="#ed4245",
            bg="#2b2d31"
        )
        self.error_label.pack(pady=(0, 10))

        # Bouton connexion
        self.login_btn = tk.Button(
            form_frame,
            text="Se connecter",
            font=("Arial", 12, "bold"),
            bg="#5865f2",
            fg="white",
            activebackground="#4752c4",
            activeforeground="white",
            relief=tk.FLAT,
            cursor="hand2",
            command=self.do_login,
            width=30,
            height=2
        )
        self.login_btn.pack(pady=10)

        # Bind Enter
        self.password_entry.bind('<Return>', lambda e: self.do_login())

        # Info
        tk.Label(
            main,
            text=f"Version {self.minecraft_version} | Serveur: {self.server_ip}",
            font=("Arial", 9),
            fg="#72767d",
            bg="#1a1b1e"
        ).pack(side=tk.BOTTOM, pady=20)

    def do_login(self):
        """Effectue la connexion via l'API"""
        email = self.email_entry.get().strip()
        password = self.password_entry.get()

        if not email or not password:
            self.error_label.config(text="Veuillez remplir tous les champs")
            return

        self.login_btn.config(state=tk.DISABLED, text="Connexion...")
        self.error_label.config(text="")
        self.window.update()

        try:
            # Appel API
            data = json.dumps({"email": email, "password": password}).encode('utf-8')
            req = urllib.request.Request(
                f"{self.api_url}/api/auth/login",
                data=data,
                headers={"Content-Type": "application/json"}
            )

            # Desactiver verification SSL pour dev
            ctx = ssl.create_default_context()
            ctx.check_hostname = False
            ctx.verify_mode = ssl.CERT_NONE

            with urllib.request.urlopen(req, timeout=10, context=ctx) as response:
                result = json.loads(response.read().decode('utf-8'))

            self.token = result.get('token')
            self.user = result.get('user')

            if self.token and self.user:
                self.save_config()
                self.setup_dashboard()
            else:
                self.error_label.config(text="Erreur de connexion")

        except urllib.error.HTTPError as e:
            try:
                error_data = json.loads(e.read().decode('utf-8'))
                self.error_label.config(text=error_data.get('error', 'Erreur de connexion'))
            except:
                self.error_label.config(text=f"Erreur HTTP {e.code}")
        except urllib.error.URLError:
            self.error_label.config(text="Impossible de contacter le serveur")
        except Exception as e:
            self.error_label.config(text=f"Erreur: {str(e)}")
        finally:
            self.login_btn.config(state=tk.NORMAL, text="Se connecter")

    def setup_dashboard(self):
        """Interface principale apres connexion"""
        self.clear_window()

        # Header
        header = tk.Frame(self.window, bg="#2b2d31", height=60)
        header.pack(fill=tk.X)
        header.pack_propagate(False)

        tk.Label(
            header,
            text="HEGEMONIA",
            font=("Arial", 20, "bold"),
            fg="#f0b90b",
            bg="#2b2d31"
        ).pack(side=tk.LEFT, padx=20, pady=15)

        # User info
        user_frame = tk.Frame(header, bg="#2b2d31")
        user_frame.pack(side=tk.RIGHT, padx=20)

        tk.Label(
            user_frame,
            text=f"Bienvenue, {self.user.get('username', 'Joueur')}",
            font=("Arial", 11),
            fg="white",
            bg="#2b2d31"
        ).pack(side=tk.LEFT, padx=10)

        tk.Button(
            user_frame,
            text="Deconnexion",
            font=("Arial", 9),
            bg="#ed4245",
            fg="white",
            relief=tk.FLAT,
            cursor="hand2",
            command=self.logout
        ).pack(side=tk.LEFT)

        # Main content
        content = tk.Frame(self.window, bg="#36393f")
        content.pack(fill=tk.BOTH, expand=True)

        # Left panel - News
        left_panel = tk.Frame(content, bg="#2b2d31", width=350)
        left_panel.pack(side=tk.LEFT, fill=tk.Y, padx=10, pady=10)
        left_panel.pack_propagate(False)

        tk.Label(
            left_panel,
            text="Actualites",
            font=("Arial", 14, "bold"),
            fg="white",
            bg="#2b2d31"
        ).pack(pady=15)

        # Charger les news
        self.load_news(left_panel)

        # Right panel - Game
        right_panel = tk.Frame(content, bg="#36393f")
        right_panel.pack(side=tk.RIGHT, fill=tk.BOTH, expand=True, padx=10, pady=10)

        # Server status
        status_frame = tk.Frame(right_panel, bg="#2b2d31", padx=20, pady=15)
        status_frame.pack(fill=tk.X, pady=10)

        tk.Label(
            status_frame,
            text="Serveur Hegemonia",
            font=("Arial", 14, "bold"),
            fg="white",
            bg="#2b2d31"
        ).pack(anchor="w")

        tk.Label(
            status_frame,
            text=f"IP: {self.server_ip}:{self.server_port}",
            font=("Arial", 11),
            fg="#b9bbbe",
            bg="#2b2d31"
        ).pack(anchor="w", pady=2)

        tk.Label(
            status_frame,
            text=f"Version: Minecraft {self.minecraft_version}",
            font=("Arial", 11),
            fg="#b9bbbe",
            bg="#2b2d31"
        ).pack(anchor="w", pady=2)

        # User stats
        stats_frame = tk.Frame(right_panel, bg="#2b2d31", padx=20, pady=15)
        stats_frame.pack(fill=tk.X, pady=10)

        tk.Label(
            stats_frame,
            text="Votre profil",
            font=("Arial", 14, "bold"),
            fg="white",
            bg="#2b2d31"
        ).pack(anchor="w")

        role_color = "#f0b90b" if self.user.get('role') == 'admin' else "#5865f2"
        tk.Label(
            stats_frame,
            text=f"Role: {self.user.get('role', 'user').upper()}",
            font=("Arial", 11, "bold"),
            fg=role_color,
            bg="#2b2d31"
        ).pack(anchor="w", pady=2)

        tk.Label(
            stats_frame,
            text=f"Email: {self.user.get('email', '')}",
            font=("Arial", 10),
            fg="#b9bbbe",
            bg="#2b2d31"
        ).pack(anchor="w", pady=2)

        # Launch button
        launch_frame = tk.Frame(right_panel, bg="#36393f")
        launch_frame.pack(expand=True)

        self.launch_btn = tk.Button(
            launch_frame,
            text="JOUER",
            font=("Arial", 24, "bold"),
            bg="#57f287",
            fg="white",
            activebackground="#3ba55c",
            activeforeground="white",
            relief=tk.FLAT,
            cursor="hand2",
            command=self.launch_game,
            width=15,
            height=2
        )
        self.launch_btn.pack(pady=20)

        self.status_label = tk.Label(
            launch_frame,
            text="Pret a jouer !",
            font=("Arial", 11),
            fg="#b9bbbe",
            bg="#36393f"
        )
        self.status_label.pack()

        # Footer
        tk.Label(
            self.window,
            text="Hegemonia Launcher v1.0 | Serveur Geopolitique Minecraft",
            font=("Arial", 9),
            fg="#72767d",
            bg="#2b2d31"
        ).pack(side=tk.BOTTOM, fill=tk.X, pady=10)

    def load_news(self, parent):
        """Charge les actualites depuis l'API"""
        try:
            req = urllib.request.Request(f"{self.api_url}/api/news")
            ctx = ssl.create_default_context()
            ctx.check_hostname = False
            ctx.verify_mode = ssl.CERT_NONE

            with urllib.request.urlopen(req, timeout=5, context=ctx) as response:
                news_list = json.loads(response.read().decode('utf-8'))

            for news in news_list[:5]:
                news_frame = tk.Frame(parent, bg="#40444b", padx=10, pady=8)
                news_frame.pack(fill=tk.X, padx=10, pady=5)

                tk.Label(
                    news_frame,
                    text=news.get('title', ''),
                    font=("Arial", 10, "bold"),
                    fg="white",
                    bg="#40444b",
                    wraplength=300,
                    anchor="w",
                    justify="left"
                ).pack(anchor="w")

                content_preview = news.get('content', '')[:100] + "..."
                tk.Label(
                    news_frame,
                    text=content_preview,
                    font=("Arial", 9),
                    fg="#b9bbbe",
                    bg="#40444b",
                    wraplength=300,
                    anchor="w",
                    justify="left"
                ).pack(anchor="w", pady=2)

        except Exception as e:
            tk.Label(
                parent,
                text="Impossible de charger les actualites",
                font=("Arial", 10),
                fg="#ed4245",
                bg="#2b2d31"
            ).pack(pady=20)

    def logout(self):
        """Deconnexion"""
        self.token = None
        self.user = None
        try:
            self.config_file.unlink()
        except:
            pass
        self.setup_login()

    def launch_game(self):
        """Lance Minecraft"""
        self.launch_btn.config(state=tk.DISABLED)
        self.status_label.config(text="Verification de Minecraft...")
        self.window.update()

        try:
            if not self.check_minecraft():
                messagebox.showinfo(
                    "Minecraft requis",
                    f"Veuillez installer Minecraft {self.minecraft_version} via le launcher officiel.\n\n"
                    f"Une fois installe, relancez ce launcher."
                )
                self.status_label.config(text="Minecraft non trouve")
                return

            self.status_label.config(text="Lancement de Minecraft...")
            self.window.update()

            self.start_minecraft()

            self.status_label.config(text="Minecraft lance ! Bon jeu !")
            self.window.after(3000, lambda: self.status_label.config(text="Pret a jouer !"))

        except Exception as e:
            messagebox.showerror("Erreur", f"Erreur lors du lancement:\n{str(e)}")
            self.status_label.config(text="Erreur - Reessayez")
        finally:
            self.launch_btn.config(state=tk.NORMAL)

    def check_minecraft(self):
        """Verifie si Minecraft est installe"""
        if sys.platform == "win32":
            minecraft_path = Path(os.getenv("APPDATA")) / ".minecraft"
        elif sys.platform == "darwin":
            minecraft_path = Path.home() / "Library" / "Application Support" / "minecraft"
        else:
            minecraft_path = Path.home() / ".minecraft"

        return minecraft_path.exists()

    def start_minecraft(self):
        """Lance Minecraft"""
        username = self.user.get('username', 'Player')

        if sys.platform == "win32":
            # Windows - essayer differentes methodes
            launcher_paths = [
                Path(os.getenv("PROGRAMFILES")) / "Minecraft Launcher" / "MinecraftLauncher.exe",
                Path(os.getenv("PROGRAMFILES(X86)")) / "Minecraft Launcher" / "MinecraftLauncher.exe",
                Path(os.getenv("APPDATA")) / ".minecraft" / "launcher" / "MinecraftLauncher.exe",
                Path(os.getenv("LOCALAPPDATA")) / "Programs" / "Minecraft Launcher" / "MinecraftLauncher.exe"
            ]

            launched = False
            for launcher_path in launcher_paths:
                if launcher_path and launcher_path.exists():
                    subprocess.Popen([str(launcher_path)])
                    launched = True
                    break

            if not launched:
                # Fallback: protocole minecraft://
                try:
                    os.startfile("minecraft://")
                    launched = True
                except:
                    pass

            if not launched:
                messagebox.showinfo(
                    "Connexion au serveur",
                    f"Lancez Minecraft manuellement et connectez-vous a:\n\n"
                    f"Serveur: {self.server_ip}\n"
                    f"Port: {self.server_port}\n\n"
                    f"Pseudo: {username}"
                )
            else:
                messagebox.showinfo(
                    "Connexion au serveur",
                    f"Minecraft se lance !\n\n"
                    f"Connectez-vous au serveur:\n"
                    f"IP: {self.server_ip}:{self.server_port}"
                )

        elif sys.platform == "darwin":
            subprocess.Popen(["open", "-a", "Minecraft"])
            messagebox.showinfo(
                "Connexion au serveur",
                f"Connectez-vous au serveur:\n{self.server_ip}:{self.server_port}"
            )

        else:
            messagebox.showinfo(
                "Connexion au serveur",
                f"Lancez Minecraft et connectez-vous a:\n\n"
                f"Serveur: {self.server_ip}:{self.server_port}\n"
                f"Pseudo: {username}"
            )

    def run(self):
        """Lance le launcher"""
        self.window.mainloop()

if __name__ == "__main__":
    launcher = HegemoniaLauncher()
    launcher.run()
