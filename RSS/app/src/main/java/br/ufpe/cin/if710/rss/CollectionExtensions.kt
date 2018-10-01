package br.ufpe.cin.if710.rss

infix fun <T> Collection<T>.sameContentWith(collection: Collection<T>?)
        = collection?.let { this.size == it.size && this.containsAll(it) }