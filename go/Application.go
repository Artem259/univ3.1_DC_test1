package main

import (
	"fmt"
	"math/rand"
	"sync"
	"time"
)

var statuses []int
var lotsN int

var currentPrice int
var currentWinner int

type Barrier struct {
	n      int
	f1, f2 bool
	w1, w2 sync.WaitGroup
	mx     sync.Mutex
}

func NewBarrier(n int) *Barrier {
	b := new(Barrier)
	b.n = n
	b.w1.Add(n)
	return b
}

func (b *Barrier) awaitFirst() {
	b.w1.Done()
	b.w1.Wait()
	b.mx.Lock()
	if b.f1 == false {
		b.f2 = false
		b.w2.Add(b.n)
		b.f1 = true
	}
	b.mx.Unlock()
}

func (b *Barrier) awaitSecond() {
	b.w2.Done()
	b.w2.Wait()
	b.mx.Lock()
	if b.f2 == false {
		b.f1 = false
		b.w1.Add(b.n)
		b.f2 = true
	}
	b.mx.Unlock()
}

func auctionManager(b *Barrier, w *sync.WaitGroup) {
	for lotI := 0; lotI < lotsN; lotI++ {
		b.awaitFirst()
		for i := 0; i < len(statuses); i++ {
			if statuses[i] > 0 {
				statuses[i] -= 1
			}
		}
		fmt.Printf(" > Winner of lot №%v is member %v with a price of %v\n", lotI, currentWinner, currentPrice)
		fmt.Printf(" > Waiting for money...\n")
		time.Sleep(time.Duration(1000+rand.Intn(2000)) * time.Millisecond)
		isPaid := rand.Intn(2)
		if isPaid == 1 {
			fmt.Printf(" > + Winner paid a money!\n")
		} else {
			fmt.Printf(" > - Winner DIDN'T pay a money! Punished.\n")
			statuses[currentWinner] += 2
		}
		currentPrice = 100
		b.awaitSecond()
	}
	w.Done()
}

func auctionMember(i int, b *Barrier, m *sync.Mutex) {
	for lotI := 0; lotI < lotsN; lotI++ {
		if statuses[i] == 0 {
			attempts := 1 + rand.Intn(4)
			for attemptI := 0; attemptI < attempts; attemptI++ {
				time.Sleep(time.Duration(2000+rand.Intn(4000)) * time.Millisecond)
				m.Lock()
				currentWinner = i
				currentPrice += 1 + rand.Intn(5)
				fmt.Printf("Member %v set price: %v\n", i, currentPrice)
				m.Unlock()
			}
		}

		b.awaitFirst()
		b.awaitSecond()
	}
}

func main() {
	const membersN = 5
	lotsN = 10

	b := NewBarrier(membersN + 1)
	m := sync.Mutex{}
	w := sync.WaitGroup{}
	w.Add(1)
	currentPrice = 100

	for i := 0; i < membersN; i++ {
		statuses = append(statuses, 0)
	}

	go auctionManager(b, &w)
	for i := 0; i < membersN; i++ {
		go auctionMember(i, b, &m)
	}

	w.Wait()
}
