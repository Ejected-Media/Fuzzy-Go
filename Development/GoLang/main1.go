package main

import (
	"context"
	"fmt"
	"log"
	"os"

	firebase "firebase.google.com/go"
	"google.golang.org/api/option"
)

func main() {
	fmt.Println("Starting Fuzzy-Go Backend...")

	// 1. Initialize Firebase/Firestore
	ctx := context.Background()
	
	// We will use a service account key later. 
	// For now, checks if we can init the app wrapper.
	opt := option.WithCredentialsFile("serviceAccountKey.json")
	app, err := firebase.NewApp(ctx, nil, opt)
	if err != nil {
		log.Printf("Warning: Firebase init failed (expected if no key yet): %v\n", err)
	} else {
		fmt.Println("Firebase App Initialized successfully.")
	}

	// 2. Placeholder for HTTP Server
	// We will likely use 'net/http' or 'chi' or 'gin' here later.
	client, err := app.Firestore(ctx)
	if err != nil {
		log.Printf("Warning: Could not get Firestore client: %v\n", err)
	} else {
		defer client.Close()
		fmt.Println("Firestore Client Connected.")
	}
	
	// Keep process alive for now (mock server)
	fmt.Println("Server ready on port 8080 (Placeholder)")
}
