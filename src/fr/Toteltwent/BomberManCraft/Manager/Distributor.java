package fr.Toteltwent.BomberManCraft.Manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Distributor<T> {
	
    private class Object<B> {
        public B Obj;
        public int Score;
        public float Percentage = 0f;
    }

    private boolean _sealed = false;
    private List<Object<T>> _objects = new ArrayList<Object<T>>();
    private int _total_score = 0;
    private Random _random = null;

    public Distributor(){
    	_random = new Random();
    }

    public Distributor(Random random){
        _random = random;
    }

    public void Add(T to_add, int score){
        _total_score += score;

        Object<T> new_obj = new Object<T>();
        new_obj.Obj = to_add;
        new_obj.Score = score;

        _objects.add(new_obj);
        _sealed = false;
    }

    public void Seal()
    {
        if (!_sealed){
            int remaining_score = _total_score;
            
            Collections.sort(_objects, new Comparator<Object<T>>(){
                @Override
                public int compare(Object<T> x, Object<T> y){
                	return y.Score - x.Score;
                }
            });

            for(Object<T> obj : _objects){
                obj.Percentage = (float)obj.Score / (float)remaining_score;
                remaining_score -= obj.Score;
            }

            _sealed = true;
        }
    }

    public T Distribute(){
    	assert _sealed : "Object_Distributor must be sealed before any call to distribute object.";

        for (Object<T> obj: _objects){
            float rand = _random.nextFloat();
            if (rand < obj.Percentage){
                return obj.Obj;
            }
        }

        return null;
    }
}